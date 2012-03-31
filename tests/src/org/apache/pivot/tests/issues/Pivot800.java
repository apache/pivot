package org.apache.pivot.tests.issues;

import java.io.File;
import java.io.FileFilter;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;

public class Pivot800 implements Application {
    private FileBrowserSheet sheet;

    public static void main(String[] args) {
        DesktopApplicationContext.main(new String[] { Pivot800.class.getName() });
    }

    public void startup(Display display, Map<String, String> properties) throws Exception {
        Window window = new Window();
        window.setMaximized(true);
        sheet = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
        sheet.getWindowStateListeners().add(new SelectFileListener());
        window.open(display);
        sheet.open(window);
    }

    private class SelectFileListener extends WindowStateListener.Adapter {
        @Override
        public void windowOpened(Window window) {
            File homeFolder = new File(System.getProperty("user.home"));
            File firstFolderInHome = homeFolder.listFiles(new FolderFilter())[0];
            sheet.setRootDirectory(homeFolder);
            System.out.println("selecting file " + firstFolderInHome);
            sheet.setSelectedFile(firstFolderInHome);
            System.out.println("Selected file is " + sheet.getSelectedFile() + ", but button state is not updated!");
        }
    }

    public boolean shutdown(boolean optional) throws Exception {
        return false;
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }

    private class FolderFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }

}