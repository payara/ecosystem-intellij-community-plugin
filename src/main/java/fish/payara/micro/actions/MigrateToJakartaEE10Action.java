package fish.payara.micro.actions;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.terminal.JBTerminalWidget;
import fish.payara.micro.PayaraMicroProject;
import fish.payara.micro.maven.MavenProject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

public class MigrateToJakartaEE10Action extends MicroAction {
    private static final Logger LOG = Logger.getLogger(MigrateToJakartaEE10Action.class.getName());

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        VirtualFile srcFile = PlatformDataKeys.VIRTUAL_FILE.getData(actionEvent.getDataContext());
        assert srcFile != null;
        FileChooserDescriptor fileDescriptor = new FileChooserDescriptor(false,true,
                false,false,false,false)
                .withTitle(srcFile.isDirectory() ? "Choose the New Project Destination Folder" :
                        "Choose the New File Destination Folder");
        VirtualFile destFolder = FileChooser.chooseFile(fileDescriptor, actionEvent.getProject(), srcFile);
        assert destFolder != null;
        String destinationPath = getDestinationPath(destFolder, srcFile);

        final Project project = CommonDataKeys.PROJECT.getData(actionEvent.getDataContext());
        assert project != null;
        PsiFile[] poms = FilenameIndex.getFilesByName(project, "pom.xml", GlobalSearchScope.projectScope(project));
        PayaraMicroProject microProject = new MavenProject(project, poms[0]);
        String projectName = project.getName();
        JBTerminalWidget terminal = getTerminal(project, projectName);
        if (terminal != null) {
            Messages.showMessageDialog(
                    "Generating: " + destinationPath,
                    "Confirmation",
                    Messages.getInformationIcon());
            final int TIME_OUT = 1000 * 60 * 5;
            executorService.execute(new Thread(() -> {
                executeCommand(terminal, microProject.getTransformCommand(srcFile.getPath(), destinationPath));
            }));
            executorService.execute(new Thread(() -> {
                Path file = Paths.get(destinationPath);
                int count = 1;
                while (!Files.exists(file)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOG.log(WARNING, e.getMessage(), projectName);
                    }
                    if (count > TIME_OUT) {
                        String message = "Migration Aborted after 5 minutes";
                        LOG.log(WARNING, message, projectName);
                        throw new RuntimeException(message);
                    }
                }
                if (srcFile.isDirectory()) {
                    ProjectUtil.openOrImport(destinationPath, project, true);
                } else {
                    VfsUtil.findFile(file, true);
                }
            }));
        } else {
            LOG.log(WARNING, "Shell window for {0} is not available.", projectName);
        }
    }

    @NotNull
    private static String getDestinationPath(VirtualFile destFolder, VirtualFile srcFile) {
        String fileName = srcFile.getName();
        if (srcFile.isDirectory()) {
            return destFolder.getPath() + "/" + fileName + "JakartaEE10";
        }
        String destPath = destFolder.getPath() + "/jakartaee10/";
        try {
            Files.createDirectories(Paths.get(destPath));
            return destPath + fileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAction(PayaraMicroProject project) {}

    @Override
    public void update(AnActionEvent e) {
        VirtualFile file = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        e.getPresentation().setEnabledAndVisible(file != null && (
                !file.isDirectory() || isRoot(file, e.getProject())
        ));
    }

    private boolean isRoot(VirtualFile file, Project project) {
        if (file != null && project != null) {
            return file.getPath().equals(project.getBasePath());
        }
        return false;
    }
}
