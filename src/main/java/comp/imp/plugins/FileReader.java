package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Response;

import java.io.*;
import java.util.Date;

public class FileReader implements IPlugin {

    private static final File WEB_ROOT = new File(".");
    private static final String DEFAULT_FILE = "index.html";
    private static final String FILE_NOT_FOUND = "404.html";

    // VERBOSE mode
    private static final boolean VERBOSE = true;


    @Override
    public float canHandle(IRequest req) {
        float abillity = BASELINE;
        if(req.getMethod().equals("GET")){
            abillity *= 1 + (0.1 * (1-abillity));
        }
        if(!req.getUrl().getFileName().equals("")){
            abillity *= 1 + (0.25 * (1-abillity));
        }
        if(!req.getUrl().getExtension().equals("")){
            abillity *= 1 + (0.15 * (1-abillity));
        }
        return abillity;
    }

    @Override
    public IResponse handle(IRequest req) {
        String method = req.getMethod();
        String fileRequested = req.getUrl().getFileName();

        System.out.println("File: "+fileRequested);
        //-------------------------------------------------------
        IResponse response = new Response();
        File file = new File(WEB_ROOT, fileRequested);
        int fileLength = (int) file.length();
        String content = getContentType(fileRequested);

        if (method.equals("GET")) // GET method so we return content
        {
            try {
                byte[] fileData = readFileData(file, fileLength);// send HTTP Headers
                response.setContent(fileData);
            } catch (FileNotFoundException fnfe) {
                try {
                    fileNotFound(response, fileRequested);
                } catch (IOException ioe) {
                    System.err.println("Error with file not found exception : " + ioe.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (VERBOSE) {
            System.out.println("File " + fileRequested + " of type " + content + " returned");
        }
        response.setServerHeader("Webio Java HTTP Server : 1.0");
        response.getHeaders().put("Date", new Date().toString());
        response.getHeaders().put("Content-type", content);
        response.getHeaders().put("Content-length", String.valueOf(fileLength));

        if (fileRequested.equals("")) {
            fileRequested += DEFAULT_FILE;
        }

        if (!method.equals("GET")  &&  !method.equals("HEAD"))
        {
            //NOT SUPPORTED ERROR!!
        } else {
            if (VERBOSE) {
                System.out.println("File " + fileRequested + " of type " + content + " returned");
            }
        }
        return response;
    }


    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];
        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }
        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }

    private void fileNotFound(IResponse response, String fileRequested) throws IOException
    {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        //String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);
        response.setContent(fileData);
        //response.setServerHeader("Webio Java HTTP Server : 1.0");
        //response.getHeaders().put("Date", new Date().toString());
        //response.getHeaders().put("Content-type", content);
        //response.getHeaders().put("Content-length", String.valueOf(fileLength));

        if (VERBOSE) {
            System.out.println("File " + fileRequested + " not found");
        }
    }

    @Override
    public String toString(){
        return "FileReader";
    }

}
