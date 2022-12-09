package fish.payara.micro.actions;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.terminal.JBTerminalWidget;
import fish.payara.micro.PayaraMicroProject;
import fish.payara.micro.maven.MavenProject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

public class MigrateToJakartaEE10Action extends MicroAction {
    private static final Logger LOG = Logger.getLogger(MigrateToJakartaEE10Action.class.getName());

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
            executeCommand(terminal, microProject.getTransformCommand(srcFile.getPath(), destinationPath));
            Messages.showMessageDialog(
                    "Generating: " + destinationPath,
                    "Confirmation",
                    Messages.getInformationIcon());
        } else {
            LOG.log(WARNING, "Shell window for {0} is not available.", projectName);
        }
        finalAction(destinationPath, project);
    }

    private static void finalAction(String destinationPath, Project project) {
        File file = new File(destinationPath);
        while (!file.exists()) {
            LOG.log(INFO, "waiting for terminal transform command");
        }
        if (file.isFile()) {
            Messages.showMessageDialog(
                    "File " + destinationPath + " created!",
                    "Finished",
                    Messages.getInformationIcon());
        } else {
            ProjectUtil.openOrImport(destinationPath, project, true);
        }
    }

    @NotNull
    private static String getDestinationPath(VirtualFile destFolder, VirtualFile srcFile) {
        String fileName = srcFile.getName();
        if (srcFile.isDirectory()) {
            return destFolder.getPath() + "/" + fileName + "-JakartaEE10";
        }
        int dotIndex = fileName.lastIndexOf(".");

        return destFolder.getPath() + "/" + fileName.substring(0, dotIndex) + "JakartaEE10"
                + fileName.substring(dotIndex);
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
