/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.vm.decompiler.swing;

/**
 *
 * @author pmikova
 */


//import com.redhat.thermostat.client.swing.internal.views.SwingVmInformationViewProvider;

import com.redhat.thermostat.client.core.views.BasicView;
import com.redhat.thermostat.client.core.views.UIComponent;
import com.redhat.thermostat.client.swing.SwingComponent;
import com.redhat.thermostat.client.swing.components.HeaderPanel;
import com.redhat.thermostat.client.swing.internal.views.SwingVmInformationViewProvider;
import com.redhat.thermostat.shared.locale.LocalizedString;
import com.redhat.thermostat.shared.locale.Translate;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class BytecodeDecompilerView extends BasicView implements SwingComponent, UIComponent {

    private JPanel guiMainFrame;
    private final CopyOnWriteArrayList<com.redhat.thermostat.common.ActionListener<DoActionClasses>> doListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<com.redhat.thermostat.common.ActionListener<DoActionBytes>> doBytesListeners = new CopyOnWriteArrayList<>();
    private static final Translate<LocaleResources> translateResources = LocaleResources.createLocalizer();

    private JPanel leftMainPanel;
    private JPanel rightMainPanel;
    private JScrollPane rightScrollPanel;
    private JScrollPane leftScrollPanel;
    private JList<String> listOfClasses;
    private JTextArea byteCodeArea;
    private HeaderPanel mainContainer;


    public BytecodeDecompilerView() {
        mainContainer = new HeaderPanel(translateResources.localize(LocaleResources.DECOMPILER_HEADER_TITLE));
        guiMainFrame = new JPanel();
        mainContainer.add(guiMainFrame, BorderLayout.CENTER);
        guiMainFrame.setLayout(new BorderLayout());

        
        listOfClasses = new JList<>();

        listOfClasses.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String name = listOfClasses.getSelectedValue();
                fireAction(DoActionBytes.BYTES, doBytesListeners, name);
            }
        ;

        }
        );
        

        JButton topButton = new JButton("Refresh loaded classes list");
        topButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireAction(DoActionClasses.CLASSES, doListeners, null);
            }
        }
        );

        byteCodeArea = new JTextArea(300, 300);

        leftMainPanel = new JPanel();
        JPanel topButtonPanel = new JPanel();
        rightMainPanel = new JPanel();

        topButtonPanel.setLayout(new BorderLayout());
        topButtonPanel.add(topButton, BorderLayout.WEST);

        rightScrollPanel = new JScrollPane(rightMainPanel);
        leftScrollPanel = new JScrollPane(leftMainPanel);

        leftMainPanel.add(listOfClasses);
        rightMainPanel.add(byteCodeArea);

        guiMainFrame.add(topButtonPanel, BorderLayout.NORTH);
        guiMainFrame.add(leftScrollPanel, BorderLayout.WEST);
        guiMainFrame.add(rightScrollPanel, BorderLayout.EAST);

        guiMainFrame.setVisible(true);

    }

    private <T extends Enum<?>> void fireAction(final T action, final java.util.List<com.redhat.thermostat.common.ActionListener<T>> listeners, String className) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    com.redhat.thermostat.common.ActionEvent<T> event = new com.redhat.thermostat.common.ActionEvent<>(this, action);
                    for (com.redhat.thermostat.common.ActionListener<T> listener : listeners) {
                        
                        listener.actionPerformed(event);
                    }
                } catch (Throwable t) {
                    //logger.log(Level.INFO, t.getMessage(), t);
                }
                return null;
            }
        }.execute();
    }

    public void reloadClassList(String[] classesToReload) {
        final String[] data = classesToReload;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                listOfClasses.setListData(data);
            }
        });

    }
    
    public void reloadTextField(String decompiledClass){
        final String data = decompiledClass;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                byteCodeArea.setText(data);
            }
        });
    }

       @Override
    public Component getUiComponent() {
        return mainContainer;
    }
// allows us to register listeners
    public enum DoActionClasses {
        CLASSES,
        
        
    }
    
    public enum DoActionBytes{
        BYTES,
    }
    
    
    
    public abstract class GetBytesActionEventActionListener implements com.redhat.thermostat.common.ActionListener<DoActionBytes> {
        String eventClassName;

        public GetBytesActionEventActionListener() {
            String eventClassName = "";
        }
        
               
        public String getEventClassName(){
            return eventClassName;
        }
        
        public void setEventClassName(String name){
            this.eventClassName = name; 
        }
        
        
        
    }

    public void addDoBytesActionListener(com.redhat.thermostat.common.ActionListener<DoActionBytes> listener) {
        doBytesListeners.add(listener);
    }
    public void addDoClassesActionListener(com.redhat.thermostat.common.ActionListener<DoActionClasses> listener) {
        doListeners.add(listener);
    }

    public void handleError(final LocalizedString message) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JOptionPane.showMessageDialog(getUiComponent().getParent(), message.getContents(), " ", JOptionPane.WARNING_MESSAGE);
            }

        });
    }

}
