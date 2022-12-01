package fish.payara.micro.actions;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.transformer.Transformer;
import org.eclipse.transformer.jakarta.JakartaTransformer;
import org.jetbrains.annotations.NotNull;

public class MigrateToJakartaEE10Action extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        VirtualFile srcFolder = PlatformDataKeys.VIRTUAL_FILE.getData(actionEvent.getDataContext());
        FileChooserDescriptor fileDescriptor = new FileChooserDescriptor(false,true,
                false,false,false,false)
                .withTitle("Choose the new Project Destination Folder");
        VirtualFile destFolder = FileChooser.chooseFile(fileDescriptor, actionEvent.getProject(), srcFolder);
        String newProjectName = srcFolder.getName() + "-JakartaEE10";
        String destinationPath = destFolder.getPath() + "/" + newProjectName;

        Transformer jTrans = new Transformer(System.out, System.err);
        jTrans.setOptionDefaults(JakartaTransformer.class, JakartaTransformer.getOptionDefaults());
        jTrans.setArgs(new String[] {srcFolder.getPath(), destinationPath});
        int rc = jTrans.run();
        if (rc == Transformer.SUCCESS_RC) {
            Messages.showMessageDialog(
                    "Project " + newProjectName + " generated!",
                    "Confirmation",
                    Messages.getInformationIcon());
        } else {
            Messages.showErrorDialog("Problem when creating " + newProjectName + " project", "Error");
        }

        ProjectUtil.openOrImport(destinationPath, actionEvent.getProject(), true);
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile file = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        e.getPresentation().setEnabledAndVisible(
                isRoot(file, e.getProject()));
    }

    private boolean isRoot(VirtualFile file, Project project) {
        return file.getPath().equals(project.getBasePath());
    }
}
