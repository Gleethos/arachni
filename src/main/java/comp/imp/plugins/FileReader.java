package comp.imp.plugins;

import comp.IPlugin;
import comp.IRequest;
import comp.IResponse;
import comp.imp.Response;

import java.io.*;
import java.util.Date;

public class FileReader implements IPlugin {

    // VERBOSE mode
    private static final boolean VERBOSE = true;

    @Override
    public float canHandle(IRequest req) {
        float abillity = BASELINE;
        abillity *= 1 + (0.5 * (1-abillity));

        if(req.getMethod().equals("GET")){
            abillity *= 1 + (0.1 * (1-abillity));
        }
        if(!req.getUrl().getFileName().equals("")){
            abillity *= 1 + (0.25 * (1-abillity));
        }
        if(!req.getUrl().getExtension().equals("")){
            abillity *= 1 + (0.15 * (1-abillity));
        }
        if(req.getUrl().getExtension().equals("")&&req.getUrl().getFileName().equals("")&&req.getMethod().equals("GET")){
            abillity *= 1 + (0.25 * (1-abillity));
        }
        if(req.getUrl().getRawUrl().equals("/")&&req.getMethod().equals("GET")){
            abillity *= 1 + (0.25 * (1-abillity));
        }
        return abillity;
    }

    @Override
    public IResponse handle(IRequest req)
    {
        String method = req.getMethod();
        String fileRequested = req.getUrl().getFileName()+"."+req.getUrl().getExtension();
        System.out.println("File: "+fileRequested);
        if(fileRequested.equals(".")){
            fileRequested = WEB_ROOT+"/index"+fileRequested+"html";
        } else {
            fileRequested = req.getUrl().getPath();
        }
        IResponse response = new Response();
        response.setStatusCode(200);
        File file = new File(".", fileRequested);
        int fileLength = (int) file.length();
        if(fileLength==0){
            File maybe = new File(WEB_ROOT+"/"+fileRequested);
            if(maybe.length()!=0){
                file = maybe;
                fileLength = (int)file.length();
            }
        }
        String content = util.getContentType(fileRequested);
        response.setServerHeader("Webio Java HTTP core.WebioServer : 1.0");
        response.getHeaders().put("date", new Date().toString());
        response.getHeaders().put("content-type", content);
        if (method.equals("GET")) // GET method so we return content
        {
            try {
                byte[] fileData = util.readFileData(file, fileLength);// send HTTP Headers
                response.setContent(fileData);
            } catch (FileNotFoundException fnfe) {
                try {
                    util.fileNotFound(response, fileRequested);
                } catch (IOException ioe) {
                    System.err.println("Error with file not found exception : " + ioe.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    @Override
    public String toString(){
        return "FileReader";
    }

}
