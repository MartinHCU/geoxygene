/*
 * This file is part of the GeOxygene project source files.
 * GeOxygene aims at providing an open framework which implements OGC/ISO
 * specifications for the development and deployment of geographic (GIS)
 * applications. It is a open source contribution of the COGIT laboratory at
 * the Institut Géographique National (the French National Mapping Agency).
 * See: http://oxygene-project.sourceforge.net
 * Copyright (C) 2005 Institut Géographique National
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with this library (see file LICENSE if present); if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package fr.ign.cogit.geoxygene.appli;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import fr.ign.cogit.geoxygene.I18N;
import fr.ign.cogit.geoxygene.feature.FT_Feature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.feature.type.GF_AttributeType;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.AttributeType;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType;
import fr.ign.cogit.geoxygene.style.Layer;
import fr.ign.cogit.geoxygene.style.StyledLayerDescriptor;

/**
 * Attribute table allowing attribute edition and visualisation.
 * <p>
 * Cette classe affiche un JDialog contenant une ou plusieurs tables qui
 * permettent de visualiser et d'éditer les attributs des objets sélectionnés.
 * @author Sylvain Becuwe
 * @author Julien Perret
 */
class AttributeTable extends JDialog {
    private static final long serialVersionUID = 1L;
    private ProjectFrame frame;
    private Set<? extends FT_Feature> objectsToDraw;
    private List<Layer> selectedLayers;
    private FT_Feature selectedFeature;
    private JTabbedPane tabPane;
    private JPopupMenu popup = new JPopupMenu();
    private JTable selectedTable;
    private MenuItemListener itemListener = new MenuItemListener();
    public ProjectFrame getProjectFrame() { return this.frame; }
    public void setSelectedFeature(FT_Feature selectedFeature) {
        this.selectedFeature = selectedFeature;
    }
    public FT_Feature getSelectedFeature() { return this.selectedFeature; }
    public void setSelectedTable(JTable selectedTable) {
        this.selectedTable = selectedTable;
    }
    public JTable getSelectedTable() { return this.selectedTable; }
    public void setItemListener(MenuItemListener itemListener) {
        this.itemListener = itemListener;
    }
    public MenuItemListener getItemListener() { return this.itemListener; }
    public void setPopup(JPopupMenu popup) { this.popup = popup; }
    public JPopupMenu getPopup() { return this.popup; }
    /**
     * Le tableModel des attributs.
     */
    public class AttributsTableModel extends DefaultTableModel{
        private static final long serialVersionUID = 1L;
        private FT_FeatureCollection<FT_Feature> features;
        private List<String> attributeNames = new ArrayList<String>();
        public void setFeatures(FT_FeatureCollection<FT_Feature> features) {
            this.features = features;
        }
        public FT_FeatureCollection<FT_Feature> getFeatures() {
            return this.features;
        }
        @SuppressWarnings("null")
        public AttributsTableModel(FT_FeatureCollection<FT_Feature> features) {
            this.setFeatures(features);
            List<GF_AttributeType> featureAttributes
            = new ArrayList<GF_AttributeType>();
            if (this.getFeatures().getFeatureType() == null) {
                //ProjectFrame.logger.info("Le featureType n'existe pas. Création d'un feature type...");
                FeatureType featureType = new FeatureType();
                String valueType = null;
                String methodName = null;
                String attributeName = null;
                AttributeType attribute = new AttributeType();
                Class<? extends FT_Feature> classe
                        = features.get(0).getClass();
                for (Method method : classe.getMethods()) {
                    if (method.getDeclaringClass() == classe) {
                        valueType = null;
                        methodName = method.getName();
                        attributeName = null;
                        attribute = new AttributeType();
                        if (methodName.startsWith("get")) { //$NON-NLS-1$
                            attributeName = methodName.substring(3);
                        } else {
                            if (method.getName().startsWith(
                            "is")) { //$NON-NLS-1$
                                attributeName = methodName.substring(2);
                            }
                        }
                        attributeName = attributeName.replace(
                                attributeName.charAt(0),
                                Character.toLowerCase(attributeName
                                        .charAt(0)));
                        if (attributeName != null) {
                            attribute.setMemberName(attributeName);
                            attribute.setNomField(attributeName);
                            Class<?> returnType = method.getReturnType();
                            valueType = returnType.getSimpleName();
                            if (returnType.isPrimitive()) {
                                if (valueType.equals("int")) { //$NON-NLS-1$
                                    valueType = "Integer"; //$NON-NLS-1$
                                } else {
                                    if (valueType.equals(
                                    "double")) { //$NON-NLS-1$
                                        valueType = "Double"; //$NON-NLS-1$
                                    } else {
                                        if (valueType.equals(
                                        "boolean")) { //$NON-NLS-1$
                                            valueType = "Boolean"; //$NON-NLS-1$
                                        } else {
                                            if (valueType.equals(
                                            "long")) { //$NON-NLS-1$
                                                valueType = "Long"; //$NON-NLS-1$
                                            }
                                        }
                                    }
                                }
                            }
                            attribute.setValueType(valueType);
                            featureType.addFeatureAttribute(attribute);
                        }
                    }			
                }
                Layer layer
                = getProjectFrame().getLayerFromFeature(features.get(0));
                for (FT_Feature ft : layer.getFeatureCollection()) {
                    ft.setFeatureType(featureType);
                }
                layer.getFeatureCollection().setFeatureType(featureType);
                featureAttributes = layer.getFeatureCollection()
                .getFeatureType().getFeatureAttributes();
            } else {
                featureAttributes = this.getFeatures().getFeatureType()
                .getFeatureAttributes();
            }
            //On récupère tout les noms d'attribut
            this.attributeNames.add("FID"); //$NON-NLS-1$
            for(GF_AttributeType attribute : featureAttributes) {
                String nomAttribut = attribute.getMemberName();
                this.attributeNames.add(nomAttribut);
            }
            //On ajoute les lignes
            for (int i = 0 ; i < features.size() ; i++) { 
                FT_Feature ft = features.get(i);
                Object[] objs = new Object[this.attributeNames.size()];
                objs[0] = new Integer(ft.getId());
                for (int j = 1 ; j < this.attributeNames.size() ; j++) {
                    objs[j] = ft.getAttribute(this.attributeNames.get(j));
                }
                insertRow(i, objs);
            }
            fireTableDataChanged();
            fireTableStructureChanged();

        }
        @Override
        public int getColumnCount() {
            return this.attributeNames != null ? this.attributeNames.size()
                    : 0;
        }
        @Override
        public int getRowCount() {
            return this.getFeatures() != null ? this.getFeatures().size() : 0;
        }
        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return new Integer(this.getFeatures().get(row).getId());
            }
            return this.getFeatures().get(row).getAttribute(
                    this.attributeNames.get(col));
        }
        @Override
        public boolean isCellEditable(int row, int col) { return col != 0; }
        @Override
        public String getColumnName(int column) {
            return this.attributeNames.get(column);
        }
        @Override
        public Class<?> getColumnClass(int column) {
            String valueType = this.getFeatures().get(0).getFeatureType()
            .getFeatureAttributeByName(this.attributeNames.get(column))
            .getValueType();
            try {
                return Class.forName("java.lang." + valueType); //$NON-NLS-1$
            }
            catch(ClassNotFoundException e) { return null; }
        }
        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col != 0) {
                boolean valOk = true;
                FT_Feature ft = this.getFeatures().get(row);
                AttributeType attributeType = ft.getFeatureType()
                .getFeatureAttributeByName(this.attributeNames.get(col));
                Class<?> type = getColumnClass(col);
                if (type == Double.class) {
                    try {
                        Double doubleValue = null;
                        if (value instanceof String) {
                            doubleValue = Double.valueOf((String) value);
                        }
                        ft.setAttribute(attributeType, doubleValue);
                        fireTableCellUpdated(row,col);
                    } catch(NumberFormatException e) { valOk = false; }
                } else {
                    if (type  == Long.class) {
                        try {
                            Long longValue = null;
                            if (value instanceof String) {
                                longValue = Long.valueOf((String) value);
                            }
                            ft.setAttribute(attributeType, longValue);
                            fireTableCellUpdated(row,col);
                        } catch(NumberFormatException e) { valOk = false; }
                    } else {
                        if (type  == Integer.class) {
                            try {
                                Integer integerValue = null;
                                if (value instanceof String) {
                                    integerValue = Integer.valueOf((String) value);
                                }
                                ft.setAttribute(attributeType, integerValue);
                                fireTableCellUpdated(row,col);
                            } catch(NumberFormatException e) { valOk = false; }
                        } else {
                            if (type  == Boolean.class) {
                                Boolean booleanValue = null;
                                if (value instanceof String) {
                                    String string = (String) value;
                                    string = string.toLowerCase();
                                    if (string.equals("false") //$NON-NLS-1$
                                            || string.equals("true")) { //$NON-NLS-1$
                                        booleanValue = Boolean.valueOf(string);
                                        ft.setAttribute(attributeType, booleanValue);
                                        fireTableCellUpdated(row,col);
                                    } else { valOk = false; }
                                }
                            } else {
                                if (type  == String.class) {
                                    ft.setAttribute(attributeType, value);
                                    fireTableCellUpdated(row,col);
                                }
                            }
                        }
                    }
                }
                if (!valOk) {
                    JOptionPane.showMessageDialog(null,
                            I18N.getString("AttributeTable." + //$NON-NLS-1$
                            "ValueNotOfType") //$NON-NLS-1$
                            + type.getSimpleName() + ".", //$NON-NLS-1$
                            I18N.getString("AttributeTable." + //$NON-NLS-1$
                            "ImpossibleToAffectValue"), //$NON-NLS-1$
                            JOptionPane.ERROR_MESSAGE);
                }
            }				
        }

    }

    /**
     *	Listener permettant d'écouter les cliques de la souris sur une ligne, une colonnes 
     *	ainsi que le clique droit.
     */
    class MouseListener extends MouseAdapter {
        private AttributsTableModel tableModel;
        private JTable table;
        private int selectedRow, selectedCol;
        public MouseListener(JTable table, AttributsTableModel tableModel) {
            this.table = table;
            this.tableModel = tableModel;
        }
        @Override
        public void mousePressed(MouseEvent e) {	    		
            //Identifie la ligne sélectionnée
            this.selectedRow = e.getY() / this.table.getRowHeight();
            //Identifie la colonne sélectionnée
            int i = 0;
            int width = 0;
            boolean found = false;
            while (i < this.tableModel.getColumnCount() && !found) {
                TableColumn col = this.table.getColumnModel().getColumn(i);
                if ((col.getWidth() + width) / e.getX() >= 1) {
                    found = true;
                } else {
                    i++;
                    width += col.getWidth();
                }
            }
            if (!found) {
                this.selectedCol = -1;
            } else { this.selectedCol = i; }
            if (this.tableModel.getRowCount() != 0) {
                this.table.clearSelection();
                if (this.selectedRow < this.tableModel.getRowCount()) {			       		
                    this.table.addRowSelectionInterval(this.selectedRow, this.selectedRow);
                    AttributeTable.this.setSelectedFeature(this.tableModel.getFeatures().get(this.selectedRow));
                    AttributeTable.this.setSelectedTable(this.table);
                }
            }
            maybeShowPopup(e, this.selectedCol);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e, this.selectedCol);
        }
        public void maybeShowPopup(MouseEvent e, int selectedCol) {
            if (e.isPopupTrigger()) {
                if (selectedCol > 0) {
                    AttributeTable.this.getItemListener()
                    .setAttributeName(this.tableModel
                            .getColumnName(selectedCol));
                    ((JMenuItem) AttributeTable.this.getPopup().
                            getComponent(0)).setText(I18N.getString(
                                    "AttributeTable." + //$NON-NLS-1$
                            "EditAttributeType") //$NON-NLS-1$
                            + AttributeTable.this.getItemListener()
                            .getAttributeName());
                    AttributeTable.this.getPopup().show(
                            (Component) e.getSource(), e.getX(), e.getY());
                }
            }
        }
    }
    /**
     * Listener des menuItem du popup menu.
     */
    class MenuItemListener implements ActionListener{
        private String attributeName;
        public String getAttributeName() { return this.attributeName; }
        public void setAttributeName(String newAttributeName) {
            this.attributeName = newAttributeName;
        }
        public void actionPerformed(ActionEvent event) {
            AttributeType attribute = AttributeTable.this.getSelectedFeature()
            .getFeatureType().getFeatureAttributeByName(this.attributeName);
            new EditTypeDialog(attribute);
        }
    }
    /**
     * Classe qui affiche un JDialog permettant de modifier le type de valeur de
     * l'attribut selectionné.
     */
    public class EditTypeDialog extends JDialog {
        private static final long serialVersionUID = 1L;
        AttributeType attribute;
        String attributeName;
        JComboBox valueType;
        public EditTypeDialog (AttributeType attribute) {
            super(getProjectFrame().getMainFrame());
            setResizable(false);
            setLocationRelativeTo(getProjectFrame().getContentPane());
            setTitle(I18N.getString("AttributeTable.ValueType")); //$NON-NLS-1$
            setLayout(new BorderLayout());
            setModal(true);
            this.attribute = attribute;
            this.attributeName = attribute.getMemberName();
            JTextArea text = new JTextArea(I18N.getString(
                    "AttributeTable.SelectValueType") //$NON-NLS-1$
                    + this.attributeName);
            text.setEditable(false);
            text.setFocusable(false);
            text.setAutoscrolls(true);
            text.setBackground(new Color(252, 233, 198));
            String[] possibleValues = {"Double", //$NON-NLS-1$
                    "Integer", //$NON-NLS-1$
                    "Long", "String", //$NON-NLS-1$ //$NON-NLS-2$
            "Boolean"}; //$NON-NLS-1$
            this.valueType = new JComboBox(possibleValues);
            this.valueType.setSelectedItem(attribute.getValueType());
            JPanel controls = new JPanel();
            JButton validateButton = new JButton(I18N.getString(
            "AttributeTable.Validate")); //$NON-NLS-1$
            validateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    validateChange();
                }
            });
            controls.add(validateButton);
            JButton cancelButton = new JButton(I18N.getString(
            "AttributeTable.Cancel")); //$NON-NLS-1$
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) { cancel(); }
            });
            controls.add(cancelButton);
            add(text, BorderLayout.NORTH);
            add(this.valueType, BorderLayout.CENTER);
            add(controls, BorderLayout.SOUTH);
            pack();
            setVisible(true);
        }
        public void validateChange() {
            final String newValueType = (String) this.valueType.getSelectedItem();
            final String oldValueType = this.attribute.getValueType();
            JTable table = AttributeTable.this.getSelectedTable();
            String layerName = table.getName();
            Layer layer = getProjectFrame().getSld().getLayer(layerName);
            FT_FeatureCollection<? extends FT_Feature> features = layer.getFeatureCollection();
            // both types are the same
            if (newValueType.equals(oldValueType)) { return; }
            // new type is String, we create a new String containing the value
            if (newValueType.equals("String")) { //$NON-NLS-1$
                for (FT_Feature ft : features) {
                    ft.setAttribute(this.attribute, new String(ft.getAttribute(
                            this.attribute).toString()));
                }
                changeAttributeType(newValueType);
                return;
            }
            // old type is Integer, we can convert values to anything
            if (oldValueType.equals("Integer")) { //$NON-NLS-1$
                try {
                    Class<?> newValueTypeClass = Class.forName(
                            "java.lang." + newValueType); //$NON-NLS-1$
                    Constructor<?> constructor = newValueTypeClass
                    .getConstructor(String.class);
                    for (FT_Feature ft : features) {
                        Integer oldValue
                        = (Integer) (ft.getAttribute(this.attributeName));
                        String stringValue = oldValue.toString();
                        if (newValueType.equals("Boolean")) { //$NON-NLS-1$
                            stringValue = (oldValue.intValue() != 0)
                            ? "true" : "false";  //$NON-NLS-1$//$NON-NLS-2$
                        }
                        Object newValue = constructor.newInstance(stringValue);
                        ft.setAttribute(this.attribute, newValue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                changeAttributeType(newValueType);
                return;
            }
            // old type is Boolean, we can convert values to anything
            if (oldValueType.equals("Boolean")) { //$NON-NLS-1$
                try {
                    Class<?> newValueTypeClass = Class.forName(
                            "java.lang." + newValueType); //$NON-NLS-1$
                    Constructor<?> constructor = newValueTypeClass
                    .getConstructor(String.class);
                    for (FT_Feature ft : features) {
                        Boolean oldValue
                        = (Boolean) (ft.getAttribute(this.attributeName));
                        Object newValue = null;
                        if (newValueType.equals("String")) { //$NON-NLS-1$
                            newValue = constructor.newInstance(oldValue
                                    .toString());
                        } else {
                            newValue = constructor.newInstance(new String(
                                    oldValue.booleanValue()
                                    ? "1" : "0")  //$NON-NLS-1$ //$NON-NLS-2$
                            );
                        }
                        ft.setAttribute(this.attribute, newValue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                changeAttributeType(newValueType);
                return;
            }
            // old type is Double
            if (oldValueType.equals("Double")) { //$NON-NLS-1$
                if (newValueType.equals("Long")) { //$NON-NLS-1$
                    for (FT_Feature ft : features) {
                        Double oldValue = (Double)(ft.getAttribute(this.attributeName));
                        Long newValue = new Long(oldValue.longValue());
                        ft.setAttribute(this.attribute, newValue);
                    }
                    changeAttributeType(newValueType);
                    return;
                }
                ConfirmChangeDialog confirmDialog = new ConfirmChangeDialog(this, oldValueType, newValueType);
                boolean confirmed = confirmDialog.isChangeConfirmed();
                if (!confirmed) { return; }
                if (newValueType.equals("Integer")) { //$NON-NLS-1$
                    for (FT_Feature ft : features) {
                        Double oldValue = (Double) (ft.getAttribute(this.attributeName));
                        Integer newValue = new Integer(oldValue.intValue());
                        ft.setAttribute(this.attribute, newValue);
                    }
                } else {
                    if (newValueType.equals("Boolean")) { //$NON-NLS-1$
                        for (FT_Feature ft : features) {
                            Double oldValue = (Double) (ft.getAttribute(this.attributeName));
                            Boolean newValue = new Boolean(oldValue.doubleValue() != 0d);
                            ft.setAttribute(this.attribute, newValue);
                        }
                    }
                }
                changeAttributeType(newValueType);
                return;
            }
            // Long
            if (oldValueType.equals("Long")) { //$NON-NLS-1$
                if (newValueType.equals("Double")) { //$NON-NLS-1$
                    for (FT_Feature ft : features) {
                        Long oldValue = (Long) (ft.getAttribute(this.attributeName));
                        Double newValue = new Double(oldValue.doubleValue());
                        ft.setAttribute(this.attribute, newValue);
                    }
                    changeAttributeType(newValueType);
                    return;
                }
                ConfirmChangeDialog confirmDialog = new ConfirmChangeDialog(this, oldValueType, newValueType);
                boolean confirmed = confirmDialog.isChangeConfirmed();
                if (!confirmed) { return; }
                if (newValueType.equals("Integer")) { //$NON-NLS-1$
                    for (FT_Feature ft : features) {
                        Long oldValue = (Long) (ft.getAttribute(this.attributeName));
                        Integer newValue = new Integer(oldValue.intValue());
                        ft.setAttribute(this.attribute, newValue);
                    }
                } else {
                    if (newValueType.equals("Boolean")) { //$NON-NLS-1$
                        for (FT_Feature ft : features) {
                            Long oldValue = (Long) (ft.getAttribute(this.attributeName));
                            Boolean newValue = new Boolean(oldValue.longValue() != 0l);
                            ft.setAttribute(this.attribute, newValue);
                        }
                    }
                }
                changeAttributeType(newValueType);
                return;
            }
            // old type is String, we try to parse values
            if (oldValueType.equals("String")) { //$NON-NLS-1$
                ConfirmChangeDialog confirmDialog = new ConfirmChangeDialog(this, oldValueType, newValueType);
                boolean confirmed = confirmDialog.isChangeConfirmed();
                if (!confirmed) { return; }
                try {
                    Class<?> newValueTypeClass = Class.forName(
                            "java.lang." + newValueType); //$NON-NLS-1$
                    Constructor<?> constructor = newValueTypeClass
                    .getConstructor(String.class);
                    for (FT_Feature ft : features) {
                        String oldValue = (String) (ft.getAttribute(this.attributeName));
                        try {
                            Object newValue = constructor.newInstance(oldValue);
                            ft.setAttribute(this.attribute, newValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ft.setAttribute(this.attribute, null);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                changeAttributeType(newValueType);
            }
        }
        private void changeAttributeType(final String newValueType) {
            this.attribute.setValueType(newValueType);
            ((DefaultTableModel)AttributeTable.this.getSelectedTable()
                    .getModel()).fireTableDataChanged();
            dispose();
        }
        public void cancel() { dispose(); }
    }
    /**
     * Constructeur de TableAttributs qui permet d'instancier le JDialog a partir de la liste
     * des objets sélectionnés du PanelVisu.
     * @param frame
     * @param title
     */
    public AttributeTable(ProjectFrame frame, String title) {
        super(frame.getMainFrame());
        this.frame = frame;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getProjectFrame().getLayerLegendPanel());
        setTitle(title);
        setSize(800, 350);
        setLayout(new BorderLayout());
        this.objectsToDraw = getProjectFrame().getLayerViewPanel().getSelectedFeatures();
        List<String> layersName = new ArrayList<String>();
        for (Layer l : getProjectFrame().getSld().getLayers()) {
            layersName.add(l.getName());
        }
        List<Layer> layers = new ArrayList<Layer>();
        this.selectedLayers = new ArrayList<Layer>();
        StyledLayerDescriptor sld = getProjectFrame().getSld();
        for (String layer : layersName) {
            layers.add(sld.getLayer(layer));
        }
        this.tabPane = new JTabbedPane(SwingConstants.LEFT);
        //On récupère les couches sélectionnées a partir des objets selectionnes
        for (Layer layer : layers) {
            FT_FeatureCollection<?> listeFeatures = layer.getFeatureCollection();
            for (FT_Feature ft : this.objectsToDraw) {
                if (listeFeatures.contains(ft)) {
                    this.selectedLayers.add(layer);
                    break;
                }
            }
        }
        initJDialog();
    }
    /**
     * Constructeur de TableAttributs qui permet d'instancier le JDialog a
     * partir d'une liste de features envoyées en paramètre.
     * @param frame
     * @param title
     * @param features La liste des features dont on veut afficher les attributs
     */
    public AttributeTable(ProjectFrame frame, String title, Set<? extends FT_Feature> features) {
        super(frame.getMainFrame());
        this.frame = frame;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(frame);
        setTitle(title);
        setSize(800, 350);
        setLayout(new BorderLayout());
        this.objectsToDraw = features;
        List<String> layersName = new ArrayList<String>();
        for (Layer l : getProjectFrame().getSld().getLayers()) {
            layersName.add(l.getName());
        }
        List<Layer> layers = new ArrayList<Layer>();
        this.selectedLayers = new ArrayList<Layer>();
        StyledLayerDescriptor sld = getProjectFrame().getSld();
        for (String layer : layersName) {
            layers.add(sld.getLayer(layer));
        }
        this.tabPane = new JTabbedPane(SwingConstants.LEFT);
        //On récupère les couches sélectionnées a partir des objets selectionnes
        for (Layer layer : layers) {
            FT_FeatureCollection<?> listeFeatures = layer.getFeatureCollection();
            for (FT_Feature ft : this.objectsToDraw) {
                if (listeFeatures.contains(ft)) {
                    this.selectedLayers.add(layer);
                    break;
                }
            }
        }
        initJDialog();
    }
    public void initJDialog() {
        for (Layer layer : this.selectedLayers) {
            JPanel tablePanel = new JPanel();
            tablePanel.setLayout(new BorderLayout());
            FT_FeatureCollection<FT_Feature> features = new FT_FeatureCollection<FT_Feature>();
            //On récupère tous les features de la couche layer qui sont sélectionnés
            FT_FeatureCollection<?> featuresOfThisLayer = layer.getFeatureCollection();
            //On récupère tous les features de la couche layer qui sont sélectionnés
            for (FT_Feature ft : this.objectsToDraw) {
                if (featuresOfThisLayer.contains(ft)) { features.add(ft); }
            }
            features.setFeatureType(layer.getFeatureCollection().getFeatureType());
            //On crée la table avec le tableModel qui correspond aux features 
            //et on l'ajoute au panel de table
            AttributsTableModel tableModel = new AttributsTableModel(features);
            JTable table = new JTable(tableModel);
            TableColumn col = table.getColumnModel().getColumn(0);
            col.setPreferredWidth(30);
            for (int i = 0 ; i < table.getColumnCount() ; i++) {
                col = table.getColumnModel().getColumn(i);
                col.setCellRenderer(new DefaultTableCellRenderer());
                DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
                col.setCellEditor(editor);
            }
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setDragEnabled(false);
            table.setFillsViewportHeight(true);
            table.setShowHorizontalLines(false);
            table.setName(layer.getName());
            MouseListener listener = new MouseListener(table, tableModel);
            table.addMouseListener(listener);
            JScrollPane scrollPane = new JScrollPane(table);
            this.tabPane.addTab(layer.getName(), scrollPane);
        }
        add(this.tabPane);
        //Panel de controle
        JPanel control = new JPanel();
        JButton centrer = new JButton(I18N.getString(
                "AttributeTable.CenterViewOnObject")); //$NON-NLS-1$
        centrer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AttributeTable.this.getSelectedFeature() != null) {
                    try {
                        getProjectFrame().getLayerViewPanel().getViewport().center(
                                AttributeTable.this.getSelectedFeature());
                    } catch (NoninvertibleTransformException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        control.add(centrer);
        JButton zoomButton = new JButton(I18N.getString(
                "AttributeTable.ZoomOnObject")); //$NON-NLS-1$
        zoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AttributeTable.this.getSelectedFeature() != null) {
                    try {
                        getProjectFrame().getLayerViewPanel().getViewport()
                        .zoom(AttributeTable.this.getSelectedFeature()
                                .getGeom().envelope());
                    } catch (NoninvertibleTransformException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        control.add(zoomButton);
        JButton closeButton = new JButton(I18N.getString(
                "AttributeTable.Close")); //$NON-NLS-1$
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { dispose(); }
        });
        control.add(closeButton);
        JMenuItem modifyValueTypeMenuItem = new JMenuItem();
        modifyValueTypeMenuItem.addActionListener(this.getItemListener());
        this.getPopup().add(modifyValueTypeMenuItem);
        add(control, BorderLayout.SOUTH);
    }
    class ConfirmChangeDialog extends JDialog {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        final JCheckBox forceChange = new JCheckBox(
                I18N.getString(
                "AttributeTable.ForceValueTypeChange")); //$NON-NLS-1$
        public ConfirmChangeDialog(Dialog owner, String oldValueType, String newValueType) {
            super(owner, I18N.getString(
            "AttributeTable.ImpossibleToConvertFrom") //$NON-NLS-1$
            + oldValueType + I18N.getString(
            "AttributeTable.ImpossibleToConvertTo") //$NON-NLS-1$
            + newValueType, true);
            Color color = new Color(255, 255, 255);
            JButton ok = new JButton("Ok"); //$NON-NLS-1$
            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ConfirmChangeDialog.this.dispose();
                }
            });
            JTextArea message = new JTextArea(I18N.getString(
                    "AttributeTable.ImpossibleToConvertFrom") //$NON-NLS-1$
                    + oldValueType + I18N.getString(
                    "AttributeTable.ImpossibleToConvertTo") //$NON-NLS-1$
                    + newValueType + ".\n"); //$NON-NLS-1$
            message.setEditable(false);
            message.setFocusable(false);
            message.setAutoscrolls(true);
            JLabel icon = new JLabel(new ImageIcon(this.getClass()
                    .getResource(
                    "/icons/64x64/warning.png"))); //$NON-NLS-1$
            this.setBackground(color);
            this.setSize(new Dimension(400, 200));
            this.setResizable(false);
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.setLayout(new BorderLayout());
            message.setBackground(this.getBackground());
            this.forceChange.setBackground(this.getBackground());
            //Panel icon
            JPanel panelIcon = new JPanel();
            panelIcon.setBackground(this.getBackground());
            panelIcon.add(icon);
            //Panel message+checkbox
            JPanel capturePanel = new JPanel();
            capturePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            capturePanel.setBackground(this.getBackground());
            capturePanel.setLayout(new BorderLayout());
            capturePanel.add(message, BorderLayout.NORTH);
            capturePanel.add(this.forceChange, BorderLayout.CENTER);
            //Panel controle
            JPanel controlPanel = new JPanel();
            controlPanel.setBackground(this.getBackground());
            controlPanel.add(ok);
            this.add(capturePanel, BorderLayout.CENTER);
            this.add(controlPanel, BorderLayout.SOUTH);
            this.add(panelIcon, BorderLayout.WEST);
            this.pack();
        }
        public boolean isChangeConfirmed() {
            this.setVisible(true);
            return this.forceChange.isSelected();
        }
    }
}
