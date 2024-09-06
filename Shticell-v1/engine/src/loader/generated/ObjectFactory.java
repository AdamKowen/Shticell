//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package loader.generated;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the generated package. 
 * <p>An ObjectFactory allows you to programmatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private static final QName _STLOriginalValue_QNAME = new QName("", "STL-Original-Value");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link STLSize }
     * 
     * @return
     *     the new instance of {@link STLSize }
     */
    public STLSize createSTLSize() {
        return new STLSize();
    }

    /**
     * Create an instance of {@link STLSheet }
     * 
     * @return
     *     the new instance of {@link STLSheet }
     */
    public STLSheet createSTLSheet() {
        return new STLSheet();
    }

    /**
     * Create an instance of {@link STLLayout }
     * 
     * @return
     *     the new instance of {@link STLLayout }
     */
    public STLLayout createSTLLayout() {
        return new STLLayout();
    }

    /**
     * Create an instance of {@link STLRanges }
     * 
     * @return
     *     the new instance of {@link STLRanges }
     */
    public STLRanges createSTLRanges() {
        return new STLRanges();
    }

    /**
     * Create an instance of {@link STLRange }
     * 
     * @return
     *     the new instance of {@link STLRange }
     */
    public STLRange createSTLRange() {
        return new STLRange();
    }

    /**
     * Create an instance of {@link STLBoundaries }
     * 
     * @return
     *     the new instance of {@link STLBoundaries }
     */
    public STLBoundaries createSTLBoundaries() {
        return new STLBoundaries();
    }

    /**
     * Create an instance of {@link STLCells }
     * 
     * @return
     *     the new instance of {@link STLCells }
     */
    public STLCells createSTLCells() {
        return new STLCells();
    }

    /**
     * Create an instance of {@link STLCell }
     * 
     * @return
     *     the new instance of {@link STLCell }
     */
    public STLCell createSTLCell() {
        return new STLCell();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "STL-Original-Value")
    public JAXBElement<String> createSTLOriginalValue(String value) {
        return new JAXBElement<>(_STLOriginalValue_QNAME, String.class, null, value);
    }

}
