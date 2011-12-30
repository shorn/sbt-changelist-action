package net.intellij.plugins.sbtchangelistaction;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class SbtChangelistActionComponent
implements
  Configurable,
  ProjectComponent
{
  public static final String COMPONENT_NAME = "SBT VCS Changelist Action";

//  private final com.intellij.openapi.diagnostic.Logger jlog = com.intellij.openapi.diagnostic.Logger.getInstance(getClass());

//  private final Logger log = LoggerFactory.getLogger(getClass());

  private Icon pluginIcon;
  protected Project project;

  public SbtChangelistActionComponent(Project project){
    this.project = project;
  }

  // ---------- Configurable ----------

  @Nls
  @Override
  public String getDisplayName() {
    return COMPONENT_NAME;
  }

  @Override
  public Icon getIcon() {
    if (pluginIcon == null) {
      pluginIcon = IconLoader.getIcon("icons/frog32.png");
    }
    return pluginIcon;
  }

  @Override
  public String getHelpTopic() {
    return null;
  }


 // ---------- ProjectComponent ----------

//  @Override
//  public void projectOpened() {
//    log.debug("projectOpened() - {}", project.getName());
//  }

//  @Override
//  public void projectClosed() {
//    log.debug("projectClosed() - " +  project.getName());
//  }

  // ---------- BaseComponent ----------

//  @Override
//  public void initComponent() {
//    log.debug("initComponent() - " +  project.getName());
//  }

//  @Override
//  public void disposeComponent() {
//    log.debug("disposeComponent() - " +  project.getName());
//  }


  // ---------- NamedComponent ----------
  @NotNull
  @Override
  public String getComponentName() {
    return "sbt-changelist-action project component";
  }


  // ---------- UnnamedConfigurable ----------

//  @Override
//  public JComponent createComponent() {
//    JPanel panel = new JPanel();
//    panel.add(new JLabel("not much here yet"));
//
//    return panel;
//  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {

  }

  @Override
  public void reset() {

  }

  @Override
  public void disposeUIResources() {

  }

}
