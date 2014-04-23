
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
 *         &lt;element name="LanguagesResult" type="{http://schemas.microsoft.com/Message}StreamBody"/>
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
    "languagesResult"
})
@XmlRootElement(name = "LanguagesResponse")
public class LanguagesResponse {

    @XmlElement(name = "LanguagesResult", required = true)
    protected byte[] languagesResult;

    /**
     * Gets the value of the languagesResult property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getLanguagesResult() {
        return languagesResult;
    }

    /**
     * Sets the value of the languagesResult property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setLanguagesResult(byte[] value) {
        this.languagesResult = value;
    }

}
