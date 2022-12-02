package fish.payara.micro.actions;

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

import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

public class MigrateToJakartaEE10Action extends MicroAction {
    private static final Logger LOG = Logger.getLogger(MigrateToJakartaEE10Action.class.getName());

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        VirtualFile srcFolder = PlatformDataKeys.VIRTUAL_FILE.getData(actionEvent.getDataContext());
        FileChooserDescriptor fileDescriptor = new FileChooserDescriptor(false,true,
                false,false,false,false)
                .withTitle("Choose the new Project Destination Folder");
        VirtualFile destFolder = FileChooser.chooseFile(fileDescriptor, actionEvent.getProject(), srcFolder);
        String newProjectName = srcFolder.getName() + "-JakartaEE10";
        String destinationPath = destFolder.getPath() + "/" + newProjectName;

        final Project project = CommonDataKeys.PROJECT.getData(actionEvent.getDataContext());
        PsiFile[] poms = FilenameIndex.getFilesByName(project, "pom.xml", GlobalSearchScope.projectScope(project));
        PayaraMicroProject microProject = new MavenProject(project, poms[0]);
        String projectName = project.getName();
        JBTerminalWidget terminal = getTerminal(project, projectName);
        if (terminal != null) {
            executeCommand(terminal, microProject.getTransformCommand(srcFolder.getPath(), destinationPath));
            Messages.showMessageDialog(
                    "Generating new Project: " + newProjectName + "!",
                    "Confirmation",
                    Messages.getInformationIcon());
        } else {
            LOG.log(WARNING, "Shell window for {0} is not available.", projectName);
        }
    }

    @Override
    public void onAction(PayaraMicroProject project) {}

    @Override
    public void update(AnActionEvent e) {
        VirtualFile file = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        e.getPresentation().setEnabledAndVisible(
                isRoot(file, e.getProject()));
    }

    private boolean isRoot(VirtualFile file, Project project) {
        if (file != null && project != null) {
            return file.getPath().equals(project.getBasePath());
        }
        return false;
    }
}
