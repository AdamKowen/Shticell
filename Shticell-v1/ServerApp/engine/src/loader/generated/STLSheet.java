//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package loader.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element ref="{}STL-Layout"/>
 *         <element ref="{}STL-Ranges" minOccurs="0"/>
 *         <element ref="{}STL-Cells"/>
 *       </sequence>
 *       <attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "stlLayout",
    "stlRanges",
    "stlCells"
})
@XmlRootElement(name = "STL-Sheet")
public class STLSheet {

    @XmlElement(name = "STL-Layout", required = true)
    protected STLLayout stlLayout;
    @XmlElement(name = "STL-Ranges")
    protected STLRanges stlRanges;
    @XmlElement(name = "STL-Cells", required = true)
    protected STLCells stlCells;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the stlLayout property.
     * 
     * @return
     *     possible object is
     *     {@link STLLayout }
     *     
     */
    public STLLayout getSTLLayout() {
        return stlLayout;
    }

    /**
     * Sets the value of the stlLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLLayout }
     *     
     */
    public void setSTLLayout(STLLayout value) {
        this.stlLayout = value;
    }

    /**
     * Gets the value of the stlRanges property.
     * 
     * @return
     *     possible object is
     *     {@link STLRanges }
     *     
     */
    public STLRanges getSTLRanges() {
        return stlRanges;
    }

    /**
     * Sets the value of the stlRanges property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLRanges }
     *     
     */
    public void setSTLRanges(STLRanges value) {
        this.stlRanges = value;
    }

    /**
     * Gets the value of the stlCells property.
     * 
     * @return
     *     possible object is
     *     {@link STLCells }
     *     
     */
    public STLCells getSTLCells() {
        return stlCells;
    }

    /**
     * Sets the value of the stlCells property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLCells }
     *     
     */
    public void setSTLCells(STLCells value) {
        this.stlCells = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
