package loader;

import sheet.api.Sheet;

import java.io.InputStream;

public interface Loader {

     Sheet loadSheetFromXML(String filePath) throws SheetLoadingException;
     Sheet loadSheetFromXML(InputStream inputStream) throws SheetLoadingException;
}
