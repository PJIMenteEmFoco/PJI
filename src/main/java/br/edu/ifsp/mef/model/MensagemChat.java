package br.edu.ifsp.mef.model;

public class MensagemChat {
    private String sender;
    private String recipient; 
    private String content;
   
    public String getSender() {
    	return sender;
    }
    public void setSender(String sender) {
    	this.sender = sender;
    }
    public String getContent() {
    	return content;
    }
    public void setContent(String content) {
    	this.content = content;
    }    
    public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

}
