/**
 * Copyright (C) 2011 Angelo Zerr <angelo.zerr@gmail.com> and Pascal Leclercq <pascal.leclercq@gmail.com>
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package fr.opensagres.xdocreport.template.formatter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.document.SyntaxKind;
import fr.opensagres.xdocreport.core.utils.StringUtils;
import fr.opensagres.xdocreport.template.registry.FieldsMetadataClassSerializerRegistry;

/**
 * Fields Metadata is used in the preprocessing step to modify some XML entries like generate script (Freemarker,
 * Velocity...) for loop for Table row, generate script for Image...
 */
public class FieldsMetadata
{

    public static final FieldsMetadata EMPTY = new FieldsMetadata();

    public static final String DEFAULT_BEFORE_ROW_TOKEN = "@before-row";

    public static final String DEFAULT_AFTER_ROW_TOKEN = "@after-row";

    public static final String DEFAULT_BEFORE_TABLE_CELL_TOKEN = "@before-cell";

    public static final String DEFAULT_AFTER_TABLE_CELL_TOKEN = "@after-cell";

    private IFieldsMetadataClassSerializer serializer;

    protected final List<FieldMetadata> fields;

    protected final Map<String, FieldMetadata> fieldsAsList;

    protected final Map<String, FieldMetadata> fieldsAsImage;

    protected final Map<String, FieldMetadata> fieldsAsTextStyling;

    private String beforeRowToken;

    private String afterRowToken;

    private String beforeTableCellToken;

    private String afterTableCellToken;

    private String description;

    private String templateEngineKind;

    public FieldsMetadata()
    {
        this( null );
    }

    public FieldsMetadata( String templateEngineKind )
    {
        this.fields = new ArrayList<FieldMetadata>();
        this.fieldsAsList = new HashMap<String, FieldMetadata>();
        this.fieldsAsImage = new HashMap<String, FieldMetadata>();
        this.fieldsAsTextStyling = new HashMap<String, FieldMetadata>();
        this.beforeRowToken = DEFAULT_BEFORE_ROW_TOKEN;
        this.afterRowToken = DEFAULT_AFTER_ROW_TOKEN;
        this.beforeTableCellToken = DEFAULT_BEFORE_TABLE_CELL_TOKEN;
        this.afterTableCellToken = DEFAULT_AFTER_TABLE_CELL_TOKEN;
        setTemplateEngineKind( templateEngineKind );
    }

    /**
     * Add a field name which is considered as an image.
     * 
     * @param fieldName
     */
    public FieldMetadata addFieldAsImage( String fieldName )
    {
        return addFieldAsImage( fieldName, fieldName );
    }

    /**
     * Add a field name which is considered as an image.
     * 
     * @param imageName
     * @param fieldName
     */
    public FieldMetadata addFieldAsImage( String imageName, String fieldName )
    {
        return addField( fieldName, null, imageName, null, null );
    }

    /**
     * Add a field name which can contains text stylink (Html, Wikipedia, etc..).
     * 
     * @param fieldName
     * @param syntaxKind
     */
    public FieldMetadata addFieldAsTextStyling( String fieldName, SyntaxKind syntaxKind )
    {
        return addFieldAsTextStyling( fieldName, syntaxKind, false );
    }

    /**
     * Add a field name which can contains text stylink (Html, Wikipedia, etc..).
     * 
     * @param fieldName
     * @param syntaxKind
     */
    public FieldMetadata addFieldAsTextStyling( String fieldName, SyntaxKind syntaxKind, boolean syntaxWithDirective )
    {
        return addFieldAsTextStyling( fieldName, syntaxKind.name(), syntaxWithDirective );
    }

    /**
     * Add a field name which can contains text stylink (Html, Wikipedia, etc..).
     * 
     * @param fieldName
     * @param syntaxKind
     */
    public FieldMetadata addFieldAsTextStyling( String fieldName, String syntaxKind )
    {
        return addFieldAsTextStyling( fieldName, syntaxKind, false );
    }

    /**
     * Add a field name which can contains text stylink (Html, Wikipedia, etc..).
     * 
     * @param fieldName
     * @param syntaxKind
     */
    public FieldMetadata addFieldAsTextStyling( String fieldName, String syntaxKind, boolean syntaxWithDirective )
    {
        // Test if it exists fields with the given name
        return addField( fieldName, null, null, syntaxKind, syntaxWithDirective );
    }

    /**
     * Add a field name which belongs to a list.
     * 
     * @param fieldName
     */
    public FieldMetadata addFieldAsList( String fieldName )
    {
        return addField( fieldName, true, null, null, null );
    }

    public FieldMetadata addField( String fieldName, Boolean listType, String imageName, String syntaxKind,
                                   Boolean syntaxWithDirective )
    {
        // Test if it exists fields with the given name
        FieldMetadata exsitingField = fieldsAsImage.get( fieldName );
        if ( exsitingField == null )
        {
            exsitingField = fieldsAsList.get( fieldName );
        }
        if ( exsitingField == null )
        {
            exsitingField = fieldsAsTextStyling.get( fieldName );
        }

        if ( exsitingField == null )
        {
            FieldMetadata fieldMetadata =
                new FieldMetadata( this, fieldName, listType == null ? false : listType, imageName, syntaxKind,
                                   syntaxWithDirective == null ? false : syntaxWithDirective );
            return fieldMetadata;
        }
        else
        {
            if ( listType != null )
            {
                exsitingField.setListType( listType );
            }
            if ( StringUtils.isNotEmpty( imageName ) )
            {
                exsitingField.setImageName( imageName );
            }
            if ( StringUtils.isNotEmpty( syntaxKind ) )
            {
                exsitingField.setSyntaxKind( syntaxKind );
            }
            if ( syntaxWithDirective != null )
            {
                exsitingField.setSyntaxWithDirective( syntaxWithDirective );
            }
            return exsitingField;
        }
    }

    /**
     * Returns list of fields name which belongs to a list.
     * 
     * @return
     */
    public Collection<String> getFieldsAsList()
    {
        return Collections.unmodifiableCollection( fieldsAsList.keySet() );
    }

    /**
     * Returns list of fields name which are considered as an image.
     * 
     * @return
     */
    public Collection<FieldMetadata> getFieldsAsImage()
    {
        return Collections.unmodifiableCollection( fieldsAsImage.values() );
    }

    /**
     * Returns list of fields name which can contains text styling.
     * 
     * @return
     */
    public Collection<FieldMetadata> getFieldsAsTextStyling()
    {
        return Collections.unmodifiableCollection( fieldsAsTextStyling.values() );
    }

    /**
     * Returns true if there are fields as image and false otherwise.
     * 
     * @return
     */
    public boolean hasFieldsAsImage()
    {
        return fieldsAsImage.size() > 0;
    }

    public boolean isFieldAsImage( String fieldName )
    {
        if ( StringUtils.isEmpty( fieldName ) )
        {
            return false;
        }
        return fieldsAsImage.containsKey( fieldName );
    }

    public String getImageFieldName( String fieldName )
    {
        if ( StringUtils.isEmpty( fieldName ) )
        {
            return null;
        }
        FieldMetadata metadata = fieldsAsImage.get( fieldName );
        if ( metadata != null )
        {
            return metadata.getFieldName();
        }
        return null;
    }

    public String getBeforeRowToken()
    {
        return beforeRowToken;
    }

    public void setBeforeRowToken( String beforeRowToken )
    {
        this.beforeRowToken = beforeRowToken;
    }

    public String getAfterRowToken()
    {
        return afterRowToken;
    }

    public void setAfterRowToken( String afterRowToken )
    {
        this.afterRowToken = afterRowToken;
    }

    public String getBeforeTableCellToken()
    {
        return beforeTableCellToken;
    }

    public void setBeforeTableCellToken( String beforeTableCellToken )
    {
        this.beforeTableCellToken = beforeTableCellToken;
    }

    public String getAfterTableCellToken()
    {
        return afterTableCellToken;
    }

    public void setAfterTableCellToken( String afterTableCellToken )
    {
        this.afterTableCellToken = afterTableCellToken;
    }

    /**
     * Returns list of fields metadata.
     * 
     * @return
     */
    public List<FieldMetadata> getFields()
    {
        return fields;
    }

    /**
     * Serialize as XML without indentation the fields metadata to the given XML writer. Here a sample of XML writer :
     * 
     * <pre>
     * <fields>
     * 	<field name="project.Name" imageName="" list="false" />
     * 	<field name="developers.Name" imageName="" list="true" />
     * <field name="project.Logo" imageName="Logo" list="false" />
     * </fields>
     * </pre>
     * 
     * @param writer
     * @throws IOException
     */
    public void saveXML( Writer writer )
        throws IOException
    {
        saveXML( writer, false );
    }

    /**
     * Serialize as XML the fields metadata to the given XML writer. Here a sample of XML writer :
     * 
     * <pre>
     * <fields>
     * 	<field name="project.Name" imageName="" list="false" />
     * 	<field name="developers.Name" imageName="" list="true" />
     * <field name="project.Logo" imageName="Logo" list="false" />
     * </fields>
     * </pre>
     * 
     * @param writer XML writer.
     * @param indent true if indent must be managed and false otherwise.
     * @throws IOException
     */
    public void saveXML( Writer writer, boolean indent )
        throws IOException
    {
        FieldsMetadataXMLSerializer.getInstance().save( this, writer, indent );
    }

    /**
     * Serialize as XML without indentation the fields metadata to the given {@link OutputStream}. Here a sample of XML
     * out:
     * 
     * <pre>
     * <fields>
     * 	<field name="project.Name" imageName="" list="false" />
     * 	<field name="developers.Name" imageName="" list="true" />
     * <field name="project.Logo" imageName="Logo" list="false" />
     * </fields>
     * </pre>
     * 
     * @param writer
     * @throws IOException
     */
    public void saveXML( OutputStream out )
        throws IOException
    {
        saveXML( out, false );
    }

    /**
     * Serialize as XML the fields metadata to the given {@link OutputStream}. Here a sample of XML out :
     * 
     * <pre>
     * <fields>
     * 	<field name="project.Name" imageName="" list="false" />
     * 	<field name="developers.Name" imageName="" list="true" />
     * <field name="project.Logo" imageName="Logo" list="false" />
     * </fields>
     * </pre>
     * 
     * @param writer XML writer.
     * @param indent true if indent must be managed and false otherwise.
     * @throws IOException
     */
    public void saveXML( OutputStream out, boolean indent )
        throws IOException
    {
        FieldsMetadataXMLSerializer.getInstance().save( this, out, indent );
    }

    /**
     * Load simple fields metadata in the given fieldsMetadata by using the given key and Java Class.
     * 
     * @param key the key (first token) to use to generate field name.
     * @param clazz the Java class model to use to load fields metadata.
     * @throws XDocReportException
     */
    public void load( String key, Class<?> clazz )
        throws XDocReportException
    {
        load( key, clazz, false );
    }

    /**
     * Load simple/list fields metadata in the given fieldsMetadata by using the given key and Java Class.
     * 
     * @param key the key (first token) to use to generate field name.
     * @param clazz the Java class model to use to load fields metadata.
     * @param listType true if it's a list and false otherwise.
     * @throws XDocReportException
     */
    public void load( String key, Class<?> clazz, boolean listType )
        throws XDocReportException
    {
        if ( serializer == null )
        {
            throw new XDocReportException(
                                           "Cannot find serializer. Please set the template engine FieldsMetadata#setTemplateEngineKind(String templateEngineKind) before calling this method." );
        }
        serializer.load( this, key, clazz, listType );
    }

    @Override
    public String toString()
    {
        StringWriter xml = new StringWriter();
        try
        {
            saveXML( xml, true );
        }
        catch ( IOException e )
        {
            return super.toString();
        }
        return xml.toString();
    }

    /**
     * Returns the description of fields metadata.
     * 
     * @return
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the description of fields metadata.
     * 
     * @param templateEngineKind
     */
    public void setDescription( String description )
    {
        this.description = description;
    }

    /**
     * Returns the template engine kind.
     * 
     * @return
     */
    public String getTemplateEngineKind()
    {
        return templateEngineKind;
    }

    /**
     * Set the template engine kind.
     * 
     * @param templateEngineKind
     */
    public void setTemplateEngineKind( String templateEngineKind )
    {
        this.templateEngineKind = templateEngineKind;
        if ( templateEngineKind == null )
        {
            serializer = null;
        }
        else
        {
            serializer = FieldsMetadataClassSerializerRegistry.getRegistry().getSerializer( templateEngineKind );
        }
    }

}
