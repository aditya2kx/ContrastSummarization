
package edu.usc.splat;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AnalyzeResult" type="{http://schemas.microsoft.com/Message}StreamBody"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "analyzeResult"
})
@XmlRootElement(name = "AnalyzeResponse")
public class AnalyzeResponse {

    @XmlElement(name = "AnalyzeResult", required = true)
    protected byte[] analyzeResult;

    /**
     * Gets the value of the analyzeResult property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getAnalyzeResult() {
        return analyzeResult;
    }

    /**
     * Sets the value of the analyzeResult property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setAnalyzeResult(byte[] value) {
        this.analyzeResult = value;
    }

}
