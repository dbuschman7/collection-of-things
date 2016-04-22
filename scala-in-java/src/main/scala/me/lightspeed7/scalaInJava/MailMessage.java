package me.lightspeed7.scalaInJava;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement
public class MailMessage {

    @XmlElementWrapper( name = "To" )
    @XmlElement( name = "ToAddress" )
    protected List<String> toAddresses;

    @XmlElement( name = "FromAddress" )
    protected String fromAddress;

    @XmlElementWrapper( name = "CC" )
    @XmlElement( name = "ccAddress" )
    protected List<String> ccAdresses;

    @XmlElement
    protected boolean isHtml;

    @XmlElement
    protected String contentType = "text/plain";

    @XmlElement
    protected String subject;

    @XmlElement
    protected String body;

    @XmlElementWrapper( name = "Attachments" )
    @XmlElement( name = "AttachmentFile" )
    protected List<String> attachmentFiles;

    public List<String> getToAddresses() {

        return toAddresses;
    }

    public void setToAddresses(List<String> toAddresses) {

        this.toAddresses = toAddresses;
    }

    public void setToAddress(String address) {

        if (this.toAddresses == null) {
            this.toAddresses = new ArrayList<String>();
        } else {
            this.toAddresses.clear();
        }
        this.toAddresses.add(address);
    }

    public void addToAddress(String address) {

        if (this.toAddresses == null) {
            this.toAddresses = new ArrayList<String>();
        }
        // Do not add duplicates.
        if (!this.toAddresses.contains(address))
            this.toAddresses.add(address);
    }

    public String getFromAddress() {

        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {

        this.fromAddress = fromAddress;
    }

    public List<String> getCcAdresses() {

        return ccAdresses;
    }

    public void setCcAdresses(List<String> ccAdresses) {

        this.ccAdresses = ccAdresses;
    }

    public void addCCAddress(String address) {

        if (this.ccAdresses == null) {
            this.ccAdresses = new ArrayList<String>();
        }
        this.ccAdresses.add(address);
    }

    public boolean isHtml() {

        return isHtml;
    }

    public void setHtml(boolean isHtml) {

        this.isHtml = isHtml;
    }

    public String getContentType() {

        return contentType;
    }

    public void setContentType(String contentType) {

        this.contentType = contentType;
    }

    public String getSubject() {

        return subject;
    }

    public void setSubject(String subject) {

        this.subject = subject;
    }

    public String getBody() {

        return body;
    }

    public void setBody(String body) {

        this.body = body;
    }

    public List<String> getAttachmentFiles() {

        return attachmentFiles;
    }

    /**
     * @param attachmentFiles
     * @throws Exception
     */
    public void setAttachmentFiles(List<String> attachmentFiles) {

        this.attachmentFiles.addAll(attachmentFiles);
    }

    public void addAttachmentFile(String data) {

        if (this.attachmentFiles == null) {
            this.attachmentFiles = new ArrayList<String>();
        }

        this.attachmentFiles.add(data);
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * A quick marshaling hack to convert to string
     * 
     * @return
     * @throws Exception
     */
    public String marshal()
            throws Exception {

        String xml = null;
        try {

            JAXBContext ctx = JAXBContext.newInstance(MailMessage.class);
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            StringWriter sw = new StringWriter();
            marshaller.marshal(this, sw);

            xml = sw.toString();
            sw.close();
            sw.flush();

        } catch (Exception ex) {
            System.out.println("ERROR : " + ex.getMessage());
            ex.printStackTrace();
        }

        return xml;
    }

}
