package eliranh.demo.Classes;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "projects")
public class Project 
{
    private Long author;
    private byte[] data;
    private String name;
    private String fileType;
    private Key key;
    private int contentLength;

    public Project(byte[] projectData, String projectName, String fileType, Key key, int contentLength)
    {
       this.data = projectData;
       this.name = projectName;
       this.fileType = fileType;
       this.key = key;
       this.contentLength = contentLength;
    }

    public Project()
    {

    }

    public void setAuthor(Long author) {
        this.author = author;
    }


    public int getContentLength() {
        return contentLength;
    }


    public String getFileType() {
        return fileType;
    }


    public void setFileType(String fileType) {
        this.fileType = fileType;
    }


    public byte[] getProjectData() {
        return data;
    }

    public void setProjectData(byte[] projectData) {
        this.data = projectData;
    }

    public String getName() {
        return name;
    }

    public void setName(String projectName) {
        this.name = projectName;
    }

    public Key getKey() {
        return key;
    }   
}
