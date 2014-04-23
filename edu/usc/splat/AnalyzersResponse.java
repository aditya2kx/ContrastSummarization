
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
 *         &lt;element name="AnalyzersResult" type="{http://schemas.microsoft.com/Message}StreamBody"/>
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
    "analyzersResult"
})
@XmlRootElement(name = "AnalyzersResponse")
public class AnalyzersResponse {

    @XmlElement(name = "AnalyzersResult", required = true)
    protected byte[] analyzersResult;

    /**
     * Gets the value of the analyzersResult property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getAnalyzersResult() {
        return analyzersResult;
    }

    /**
     * Sets the value of the analyzersResult property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setAnalyzersResult(byte[] value) {
        this.analyzersResult = value;
    }

}
