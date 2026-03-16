package com.xhait.ti.cmdb;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class DependencyReportLauncher {

    private DependencyReportLauncher() {
    }

    public static void main(String[] args) throws Exception {
        if (Boolean.parseBoolean(System.getProperty("launch.dependency-report.skip", "false"))) {
            System.out.println("Dependency report launch skipped=true; reason=launch-disabled");
            return;
        }

        if (args.length == 0 || args[0] == null || args[0].isBlank()) {
            System.out.println("Dependency report launch skipped=true; reason=missing-report-path");
            return;
        }

        Path reportPath = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.exists(reportPath)) {
            System.out.println("Dependency report launch skipped=true; reason=missing-report-file; report=" + reportPath);
            return;
        }

        if (tryDesktopBrowse(reportPath)) {
            System.out.println("Dependency report launch skipped=false; strategy=java.awt.Desktop; report=" + reportPath);
            return;
        }

        if (tryWindowsStart(reportPath)) {
            System.out.println("Dependency report launch skipped=false; strategy=cmd-start; report=" + reportPath);
            return;
        }

        System.out.println("Dependency report launch skipped=true; reason=no-supported-launch-strategy; report=" + reportPath);
    }

    private static boolean tryDesktopBrowse(Path reportPath) {
        try {
            if (!Desktop.isDesktopSupported()) {
                return false;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                return false;
            }
            desktop.browse(reportPath.toUri());
            return true;
        } catch (UnsupportedOperationException | SecurityException | IOException ex) {
            return false;
        }
    }

    private static boolean tryWindowsStart(Path reportPath) {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!osName.contains("win")) {
            return false;
        }

        try {
            Process process = new ProcessBuilder("cmd", "/c", "start", "", reportPath.toString())
                    .inheritIO()
                    .start();
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}