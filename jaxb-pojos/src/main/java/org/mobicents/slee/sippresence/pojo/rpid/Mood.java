//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.04.25 at 12:01:52 AM WEST 
//


package org.mobicents.slee.sippresence.pojo.rpid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.mobicents.slee.sippresence.pojo.commonschema.Empty;
import org.mobicents.slee.sippresence.pojo.commonschema.NoteT;
import org.w3c.dom.Element;


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
 *         &lt;element name="note" type="{urn:ietf:params:xml:ns:pidf:rpid}Note_t" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="unknown" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *           &lt;sequence maxOccurs="unbounded">
 *             &lt;choice>
 *               &lt;element name="afraid" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="amazed" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="angry" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="annoyed" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="anxious" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="ashamed" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="bored" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="brave" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="calm" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="cold" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="confused" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="contented" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="cranky" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="curious" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="depressed" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="disappointed" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="disgusted" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="distracted" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="embarrassed" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="excited" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="flirtatious" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="frustrated" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="grumpy" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="guilty" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="happy" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="hot" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="humbled" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="humiliated" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="hungry" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="hurt" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="impressed" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="in_awe" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="in_love" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="indignant" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="interested" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="invincible" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="jealous" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="lonely" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="mean" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="moody" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="nervous" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="neutral" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="offended" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="playful" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="proud" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="relieved" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="remorseful" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="restless" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="sad" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="sarcastic" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="serious" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="shocked" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="shy" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="sick" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="sleepy" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="stressed" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="surprised" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="thirsty" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="worried" type="{urn:ietf:params:xml:ns:pidf:rpid}empty"/>
 *               &lt;element name="other" type="{urn:ietf:params:xml:ns:pidf:rpid}Note_t"/>
 *               &lt;any/>
 *             &lt;/choice>
 *           &lt;/sequence>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{urn:ietf:params:xml:ns:pidf:rpid}fromUntil"/>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "note",
    "unknown",
    "afraidOrAmazedOrAngry"
})
@XmlRootElement(name = "mood")
public class Mood {

    protected NoteT note;
    protected Empty unknown;
    @XmlElementRefs({
        @XmlElementRef(name = "impressed", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "jealous", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "shy", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "guilty", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "mean", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "offended", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "cold", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "surprised", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "worried", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "humiliated", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "hungry", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "grumpy", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "playful", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "relieved", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "contented", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "serious", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "humbled", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "neutral", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "interested", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "ashamed", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "depressed", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "excited", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "distracted", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "brave", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "stressed", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "flirtatious", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "sleepy", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "amazed", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "proud", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "disgusted", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "hot", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "remorseful", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "calm", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "in_love", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "confused", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "restless", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "thirsty", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "in_awe", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "nervous", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "invincible", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "embarrassed", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "indignant", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "lonely", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "annoyed", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "hurt", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "other", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "happy", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "anxious", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "afraid", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "cranky", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "disappointed", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "sarcastic", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "sad", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "moody", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "bored", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "frustrated", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "shocked", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "curious", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "sick", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class),
        @XmlElementRef(name = "angry", namespace = "urn:ietf:params:xml:ns:pidf:rpid", type = JAXBElement.class)
    })
    @XmlAnyElement(lax = true)
    protected List<Object> afraidOrAmazedOrAngry;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar from;
    @XmlAttribute
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar until;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the note property.
     * 
     * @return
     *     possible object is
     *     {@link NoteT }
     *     
     */
    public NoteT getNote() {
        return note;
    }

    /**
     * Sets the value of the note property.
     * 
     * @param value
     *     allowed object is
     *     {@link NoteT }
     *     
     */
    public void setNote(NoteT value) {
        this.note = value;
    }

    /**
     * Gets the value of the unknown property.
     * 
     * @return
     *     possible object is
     *     {@link Empty }
     *     
     */
    public Empty getUnknown() {
        return unknown;
    }

    /**
     * Sets the value of the unknown property.
     * 
     * @param value
     *     allowed object is
     *     {@link Empty }
     *     
     */
    public void setUnknown(Empty value) {
        this.unknown = value;
    }

    /**
     * Gets the value of the afraidOrAmazedOrAngry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the afraidOrAmazedOrAngry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAfraidOrAmazedOrAngry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link Object }
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link Element }
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link NoteT }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * 
     * 
     */
    public List<Object> getAfraidOrAmazedOrAngry() {
        if (afraidOrAmazedOrAngry == null) {
            afraidOrAmazedOrAngry = new ArrayList<Object>();
        }
        return this.afraidOrAmazedOrAngry;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFrom(XMLGregorianCalendar value) {
        this.from = value;
    }

    /**
     * Gets the value of the until property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUntil() {
        return until;
    }

    /**
     * Sets the value of the until property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUntil(XMLGregorianCalendar value) {
        this.until = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
