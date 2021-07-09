package utils;
import simulator.CirSim;

public class ImportExportDialogFactory {
	public static ImportExportDialog Create(CirSim f, ImportExportDialog.Action type) {
		if (f.getApplet() != null) {
			try {
				return new ImportExportAppletDialog(f, type);
			} catch (Exception e) {
				return new ImportExportClipboardDialog(f, type);
			}
		} else {
			return new ImportExportFileDialog(f, type);
		}
	}
}