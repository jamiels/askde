//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.08.28 at 01:07:27 AM EDT 
//


package bindings.askde.listings;

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
 *         &lt;element ref="{}Location"/>
 *         &lt;element ref="{}ListingDetails"/>
 *         &lt;element ref="{}RentalDetails" minOccurs="0"/>
 *         &lt;element ref="{}BasicDetails"/>
 *         &lt;element ref="{}Pictures"/>
 *         &lt;element ref="{}Agent"/>
 *         &lt;element ref="{}Office"/>
 *         &lt;element ref="{}OpenHouses"/>
 *         &lt;element ref="{}Neighborhood"/>
 *         &lt;element ref="{}RichDetails"/>
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
    "location",
    "listingDetails",
    "rentalDetails",
    "basicDetails",
    "pictures",
    "agent",
    "office",
    "openHouses",
    "neighborhood",
    "richDetails"
})
@XmlRootElement(name = "Listing")
public class Listing {

    @XmlElement(name = "Location", required = true)
    protected Location location;
    @XmlElement(name = "ListingDetails", required = true)
    protected ListingDetails listingDetails;
    @XmlElement(name = "RentalDetails")
    protected RentalDetails rentalDetails;
    @XmlElement(name = "BasicDetails", required = true)
    protected BasicDetails basicDetails;
    @XmlElement(name = "Pictures", required = true)
    protected Pictures pictures;
    @XmlElement(name = "Agent", required = true)
    protected Agent agent;
    @XmlElement(name = "Office", required = true)
    protected Office office;
    @XmlElement(name = "OpenHouses", required = true)
    protected OpenHouses openHouses;
    @XmlElement(name = "Neighborhood", required = true)
    protected Neighborhood neighborhood;
    @XmlElement(name = "RichDetails", required = true)
    protected RichDetails richDetails;

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link Location }
     *     
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link Location }
     *     
     */
    public void setLocation(Location value) {
        this.location = value;
    }

    /**
     * Gets the value of the listingDetails property.
     * 
     * @return
     *     possible object is
     *     {@link ListingDetails }
     *     
     */
    public ListingDetails getListingDetails() {
        return listingDetails;
    }

    /**
     * Sets the value of the listingDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListingDetails }
     *     
     */
    public void setListingDetails(ListingDetails value) {
        this.listingDetails = value;
    }

    /**
     * Gets the value of the rentalDetails property.
     * 
     * @return
     *     possible object is
     *     {@link RentalDetails }
     *     
     */
    public RentalDetails getRentalDetails() {
        return rentalDetails;
    }

    /**
     * Sets the value of the rentalDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link RentalDetails }
     *     
     */
    public void setRentalDetails(RentalDetails value) {
        this.rentalDetails = value;
    }

    /**
     * Gets the value of the basicDetails property.
     * 
     * @return
     *     possible object is
     *     {@link BasicDetails }
     *     
     */
    public BasicDetails getBasicDetails() {
        return basicDetails;
    }

    /**
     * Sets the value of the basicDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link BasicDetails }
     *     
     */
    public void setBasicDetails(BasicDetails value) {
        this.basicDetails = value;
    }

    /**
     * Gets the value of the pictures property.
     * 
     * @return
     *     possible object is
     *     {@link Pictures }
     *     
     */
    public Pictures getPictures() {
        return pictures;
    }

    /**
     * Sets the value of the pictures property.
     * 
     * @param value
     *     allowed object is
     *     {@link Pictures }
     *     
     */
    public void setPictures(Pictures value) {
        this.pictures = value;
    }

    /**
     * Gets the value of the agent property.
     * 
     * @return
     *     possible object is
     *     {@link Agent }
     *     
     */
    public Agent getAgent() {
        return agent;
    }

    /**
     * Sets the value of the agent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Agent }
     *     
     */
    public void setAgent(Agent value) {
        this.agent = value;
    }

    /**
     * Gets the value of the office property.
     * 
     * @return
     *     possible object is
     *     {@link Office }
     *     
     */
    public Office getOffice() {
        return office;
    }

    /**
     * Sets the value of the office property.
     * 
     * @param value
     *     allowed object is
     *     {@link Office }
     *     
     */
    public void setOffice(Office value) {
        this.office = value;
    }

    /**
     * Gets the value of the openHouses property.
     * 
     * @return
     *     possible object is
     *     {@link OpenHouses }
     *     
     */
    public OpenHouses getOpenHouses() {
        return openHouses;
    }

    /**
     * Sets the value of the openHouses property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenHouses }
     *     
     */
    public void setOpenHouses(OpenHouses value) {
        this.openHouses = value;
    }

    /**
     * Gets the value of the neighborhood property.
     * 
     * @return
     *     possible object is
     *     {@link Neighborhood }
     *     
     */
    public Neighborhood getNeighborhood() {
        return neighborhood;
    }

    /**
     * Sets the value of the neighborhood property.
     * 
     * @param value
     *     allowed object is
     *     {@link Neighborhood }
     *     
     */
    public void setNeighborhood(Neighborhood value) {
        this.neighborhood = value;
    }

    /**
     * Gets the value of the richDetails property.
     * 
     * @return
     *     possible object is
     *     {@link RichDetails }
     *     
     */
    public RichDetails getRichDetails() {
        return richDetails;
    }

    /**
     * Sets the value of the richDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link RichDetails }
     *     
     */
    public void setRichDetails(RichDetails value) {
        this.richDetails = value;
    }

}
