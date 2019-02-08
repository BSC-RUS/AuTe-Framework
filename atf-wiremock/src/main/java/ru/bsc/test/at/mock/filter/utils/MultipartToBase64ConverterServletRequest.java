package ru.bsc.test.at.mock.filter.utils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.eclipse.jetty.http.HttpHeader;


@Slf4j
public class MultipartToBase64ConverterServletRequest extends HttpServletRequestWrapper {

    public final static String NEW_LINE = "\r\n";
    public final static String DOUBLE_DASH = "--";
    public final static String EVAL_FIELD = "|eval_field|";

    public final static String EQUAL_SIGN = "=";
    public final static String BOUNDARY = "boundary";
    public final static String SEMICOLON = ";";
    public static final String MULTIPART_TYPE    = "multipart/";
    public static final String DEFAULT_CHARACTER_ENCODING = "utf-8";
    public static final String tmpDirPath;
    public static final File tmpDir;

    private byte[] rawData;
    private MultiMap params;
    private ConfigProperties configProperties;
    private Map<String, String> headerMap = new HashMap<String, String>();



    static {

        tmpDirPath = System.getProperty("java.io.tmpdir") + File.separator + "servlet-file-upload";
        tmpDir = new File(tmpDirPath);
        if (!tmpDir.exists() && !tmpDir.mkdirs()){
            throw new RuntimeException("Failed to create temp directory " + tmpDir.getAbsolutePath());
        }
        log.info(MultipartToBase64ConverterServletRequest.class.getSimpleName() + " loaded with temp directory " + tmpDir.getAbsolutePath());
    }


    public MultipartToBase64ConverterServletRequest(HttpServletRequest httpRequest, ConfigProperties configProperties) {
        super(httpRequest);
        this.configProperties = configProperties;
    }

    public void addHeader(String name, String value) {
        headerMap.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        String headerValue = super.getHeader(name);
        if (headerMap.containsKey(name)) {
            headerValue = headerMap.get(name);
        }
        return headerValue;
    }


    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        for (String name : headerMap.keySet()) {
            names.add(name);
        }
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = new ArrayList<>();
        if (headerMap.containsKey(name)) {
            values.add(headerMap.get(name));
            return Collections.enumeration(values);
        }
        return Collections.enumeration(Collections.list(super.getHeaders(name)));
    }

    @Override
    public String getParameter(String name){
        return getParams().getValue(name);
    }

    @Override
    public Map getParameterMap(){
        return getParams().getMap();
    }

    public Enumeration<String> getParameterNames(){

        return Collections.enumeration(getParams().getMap().keySet());
    }

    public String[] getParameterValues(String name){

        List list = getParams().getValues(name);
        List<String> listOfStrings = new ArrayList(list.size());
        for (Object o : list)
            listOfStrings.add(o.toString());

        return listOfStrings.toArray(new String[listOfStrings.size()]);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {

        if (rawData == null) {
            initialize();
        }
        return new ByteArrayServletInputStream((rawData));
    }

    @Override
    public BufferedReader getReader() throws IOException {

        if (rawData == null) {
            initialize();
        }
        return new BufferedReader(new InputStreamReader(new ByteArrayServletInputStream((rawData))));
    }

    private MultiMap getParams(){

        if (rawData == null) {
            initialize();
        }
        return this.params;
    }


    private List<FileItem> settingsLoadAndParseRequest() throws FileUploadException {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // files larger than Threshold will be saved to tmpDir on disk
        factory.setSizeThreshold(configProperties.getTmpThresholdSize());
        factory.setRepository(tmpDir);

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(configProperties.getFilesThresholdSize());

        ServletRequestContext uploadContext = new ServletRequestContext(this);
        return upload.parseRequest(uploadContext);
    }


    private void initialize(){
        if (rawData != null) {
            return;
        }
        ServletRequest request = super.getRequest();
        try {
            rawData = ServletUtils.readBytes(request.getInputStream());
            String encoding = request.getCharacterEncoding();
            if (encoding == null) {
                encoding = DEFAULT_CHARACTER_ENCODING;
            }
            params = new MultiMap();
            List<FileItem> fileItems = settingsLoadAndParseRequest();
            long sum = 0;
            ConvertedRequestBody convertedRequestBody = new ConvertedRequestBody(request, configProperties.isStaticBoundaryEnabled());
            for (FileItem fileItem : fileItems){
                if (fileItem.isFormField()){
                    this.params.add(fileItem.getFieldName(), fileItem.getString(encoding));
                    log.info(">>>> " + fileItem);
                } else {
                    this.params.add(fileItem.getFieldName(), fileItem);
                    log.info(">>>> " + fileItem);
                }
                sum += fileItem.getSize();
                String partBody = buildPartMultipartRequest(fileItem);
                if(!partBody.isEmpty()) {
                    convertedRequestBody.getAllDataBody().append(partBody);
                }
            }
            processEvalField(fileItems, convertedRequestBody, sum);

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void processEvalField(List<FileItem> fileItems, ConvertedRequestBody convertedRequestBody, long seed) {
        if(fileItems.size() > 0 && convertedRequestBody.getAllDataBody().length() > 0) {
            convertedRequestBody.setStaticBoundarySeed(seed);
            convertedRequestBody.getAllDataBody()
                    .append(DOUBLE_DASH + convertedRequestBody.getStaticBoundary() + DOUBLE_DASH);

            if(configProperties.isStaticBoundaryEnabled()) {
                changeMultipartHeaderBoundary(convertedRequestBody.getStaticBoundary());
            }
            rawData = convertedRequestBody.getAllDataBody().toString().replace(EVAL_FIELD, convertedRequestBody.getStaticBoundary()).getBytes();
        }
    }

    private String buildPartMultipartRequest(FileItem fileItem) {
        return buildPartMultipartRequest(fileItem, EVAL_FIELD);
    }

    private String buildPartMultipartRequest(FileItem fileItem, String evalFieldLabeld) {
        return buildPartMultipartRequest(evalFieldLabeld, fileItem);
    }

    private void changeMultipartHeaderBoundary(String staticBoundary) {
        String[] partsContentType = getRequest().getContentType().split(SEMICOLON);
        for(int i = 0; i < partsContentType.length; i++) {
            if(partsContentType[i].trim().startsWith(BOUNDARY)) {
                partsContentType[i] = BOUNDARY + EQUAL_SIGN + staticBoundary;
            }
        }
        StringBuilder contentType = new StringBuilder();
        for(int i = 0; i < partsContentType.length; i++) {
            contentType.append(partsContentType[i]);
            if(i+1 < partsContentType.length) {
                contentType.append(SEMICOLON);
            }
        }
        addHeader(HttpHeader.CONTENT_TYPE.toString(), contentType.toString());
    }

    private String buildPartMultipartRequest(String staticBoundary, FileItem fileItem) {
        StringBuffer multipart = new StringBuffer();
        Iterator<String> headerIter = fileItem.getHeaders().getHeaderNames();
        multipart.append(DOUBLE_DASH + staticBoundary)
                .append(NEW_LINE);
        int countHeaderIterm = 0;
        while (headerIter.hasNext()) {
            String headerProp = headerIter.next();
            multipart.append(headerProp)
                    .append(EQUAL_SIGN)
                    .append(fileItem.getHeaders().getHeader(headerProp))
                    .append(NEW_LINE);
            countHeaderIterm++;
        }
        multipart.append(NEW_LINE);
        byte[] file = fileItem.get();

        if(!org.apache.commons.codec.binary.Base64.isArrayByteBase64(file)) {
            multipart.append(new String(Base64.getEncoder().encode(file)));
        } else {
            multipart.append(new String(file));
        }
        multipart.append(NEW_LINE);
        if(file.length == 0 && countHeaderIterm == 0) {
            return "";
        }
        return multipart.toString();
    }

    private class ByteArrayServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        public ByteArrayServletInputStream(byte[] data){
            inputStream = new ByteArrayInputStream(data);
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return inputStream.available() > 0;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            try {
                readListener.onDataAvailable();
            } catch (IOException ex){

            }
        }
    }


}