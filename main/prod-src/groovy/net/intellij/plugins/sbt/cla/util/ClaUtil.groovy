package net.intellij.plugins.sbt.cla.util

import javax.swing.Icon

import javax.swing.ImageIcon
import com.intellij.openapi.vcs.changes.ChangeList
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vcs.changes.Change
import com.google.common.collect.Lists
import com.google.common.base.Function
import com.intellij.openapi.roots.ProjectFileIndex
import com.google.common.base.Joiner
import com.intellij.openapi.diagnostic.Logger
import java.lang.reflect.Method
import java.lang.annotation.Annotation

/**
 * Lot of stuff in here was just lifted from the java version, needs
 * groovification.
 */
class ClaUtil {
  private final static Logger log = Logger.getInstance(ClaUtil)

  static final String ICON_LOCATION =
    "/net/intellij/plugins/sbt/cla/icons"

  private static final String VFS_FILE_SEPARTOR = "/";
  private static final String LINE_SEPARATOR =
    System.getProperty("line.separator");


  static final Map<String, Icon> icons = [:]

  // thinka bout factoring out an icon-related class instead of calling
  // this method all over the place with hardcoded strings
  static Icon getIcon(String name){
    if( icons[name] == null ){
      icons[name] = new ImageIcon(ClaUtil.getResource("$ICON_LOCATION/$name"))
    }

    return icons[name]
  }


  static Icon getIcon32() {
    return getIcon("frog32.png")
  }

  static Icon getIcon16() {
    return getIcon("frog16.png")
  }

  static String getThreadName(){
    return Thread.currentThread().name
  }

  public static ChangeList[] getSelectedChangelists(DataContext dataContext) {
    return VcsDataKeys.CHANGE_LISTS.getData(dataContext);
  }

  /**
   * creates a unique list of filenames from the given changelist files.
   *
   * @param absolutePath if this is false then the paths returned will
   * be relative from their respective content root (as determined by the
   * <tt>fileIndex</tt>)
   */
  public static Set<String> createFilenamesFromChangedFiles(
    List<VirtualFile> changedFiles,
    ProjectFileIndex fileIndex,
    boolean absolutePath)
  {
    LinkedHashSet<String> allFiles =
      new LinkedHashSet<String>(changedFiles.size());

    for( VirtualFile changeFile : changedFiles ){
      if( changeFile == null ){
        // null value in changed files, think this might be a removed file
        // needs more testing, in my use-case: what does a diff
        // for a deleted file look like?
        continue;
      }
      VirtualFile contentRootForFile =
        fileIndex.getContentRootForFile(changeFile);

      String path = changeFile.getPath();
      if( !absolutePath ){
        path = getRelativePathFromContentRoot(changeFile, contentRootForFile);
      }

      if(!allFiles.contains(path)){
        allFiles.add(path);
      }
    }

    return allFiles;
  }

  public static String getRelativePathFromContentRoot(
    VirtualFile changeFile, VirtualFile contentRootForFile)
  {
    String path = changeFile.getPath();
    if( changeFile.getPath().startsWith(contentRootForFile.getPath()) ){
      path = path.substring(contentRootForFile.getPath().length());
    }

    if( path.startsWith(VFS_FILE_SEPARTOR) ){
      path = path.substring( VFS_FILE_SEPARTOR.length() );
    }

    return path;
  }

  /**
   * this could be better, maybe replace all alhpa-numerics?
   *
   */
  public static String cleanFilename(String changelistName){
    String name = changelistName.replace(" ", "_");
    name = name.replace(",", "");
    name = name.replace("/", "_");
    name = name.replace("\\", "_" );
    name = name.replace(":", "_" );

    // I'm always writing stuff like "PTR - 1234",
    // which turns in to "PTR_-_1234", blech
    name = name.replace("_-_", "-" );

    return name
  }

  /**
   * write the content to a temp file, one per line
   * @return the file written to (already closed), or null if any error.
   */
  static File writeLnToTempFile(List<String> content) {
    String contentString = content.join(LINE_SEPARATOR)

    File temp;
    try {
      temp = File.createTempFile("idea-", "-changes");
      temp.deleteOnExit();
      BufferedWriter out = new BufferedWriter(new FileWriter(temp));
      out.write(contentString);
      out.close();
    } catch (IOException ioex) {
      log.error("Error creating temp file.", ioex);
      return null;
    }
    return temp;
  }

  static Method getMethodForProperty(
    Class<?> clazz,
    String propName,
    Class<?> propType)
  {
    String getterName = MetaProperty.getGetterName(propName, propType)
    clazz.getMethod(getterName, [] as Class[])
  }

  static Map<String, Method> getMethodsForPropertiesWithAnnotation(
    Class<?> clazz,
    Class<? extends Annotation> annoClass)
  {
    def result = [:]
    clazz.metaClass.properties.each { metaProperty ->
      Method m = getMethodForProperty(
        clazz, metaProperty.name, metaProperty.type)
      m.annotations.each { anno ->
        if( annoClass.isInstance(anno) ){
          result[metaProperty.name] = m
        }
      }
    }

    result
  }
}
