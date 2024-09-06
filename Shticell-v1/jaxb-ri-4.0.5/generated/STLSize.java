//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
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
 *       <attribute name="rows-height-units" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       <attribute name="column-width-units" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "STL-Size")
public class STLSize {

    @XmlAttribute(name = "rows-height-units", required = true)
    protected int rowsHeightUnits;
    @XmlAttribute(name = "column-width-units", required = true)
    protected int columnWidthUnits;

    /**
     * Gets the value of the rowsHeightUnits property.
     * 
     */
    public int getRowsHeightUnits() {
        return rowsHeightUnits;
    }

    /**
     * Sets the value of the rowsHeightUnits property.
     * 
     */
    public void setRowsHeightUnits(int value) {
        this.rowsHeightUnits = value;
    }

    /**
     * Gets the value of the columnWidthUnits property.
     * 
     */
    public int getColumnWidthUnits() {
        return columnWidthUnits;
    }

    /**
     * Sets the value of the columnWidthUnits property.
     * 
     */
    public void setColumnWidthUnits(int value) {
        this.columnWidthUnits = value;
    }

}
