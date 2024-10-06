package loader;

import sheet.api.Sheet;

public interface Loader {

     Sheet loadSheetFromXML(String filePath) throws SheetLoadingException;

}
