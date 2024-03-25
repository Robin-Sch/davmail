/*
 * DavMail POP/IMAP/SMTP/CalDav/LDAP Exchange Gateway
 * Copyright (C) 2010  Mickael Guessant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package davmail.exchange.auth;

import davmail.BundleMessage;
import davmail.ui.browser.DesktopBrowser;
import davmail.ui.tray.DavGatewayTray;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.net.URISyntaxException;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class O365ManualAuthenticatorDialog extends JDialog {
    final JTextField codeField = new JTextField(30);
    protected String code;

    /**
     * Get Oauth authentication code.
     *
     * @return authentication code
     */
    public String getCode() {
        if (code != null && code.contains("code=") && code.contains("&session_state=")) {
            code = code.substring(code.indexOf("code=")+5, code.indexOf("&session_state="));
        }
        return code;
    }

    /**
     * Get credentials.
     *
     * @param initUrl Kerberos prompt from callback handler
     */
    public O365ManualAuthenticatorDialog(String initUrl) {
        setAlwaysOnTop(true);

        setTitle(BundleMessage.format("UI_O365_MANUAL_PROMPT"));

        try {
            setIconImages(DavGatewayTray.getFrameIcons());
        } catch (NoSuchMethodError error) {
            DavGatewayTray.debug(new BundleMessage("LOG_UNABLE_TO_SET_ICON_IMAGE"));
        }

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));

        JLabel imageLabel = new JLabel();
        imageLabel.setIcon(UIManager.getIcon("OptionPane.questionIcon"));
        messagePanel.add(imageLabel);

        messagePanel.add(getEditorPane(BundleMessage.format("UI_0365_AUTHENTICATION_PROMPT", initUrl)));


        JPanel credentialPanel = new JPanel();
        credentialPanel.setLayout(new BoxLayout(credentialPanel, BoxLayout.X_AXIS));

        JLabel promptLabel = new JLabel(BundleMessage.format("UI_0365_AUTHENTICATION_CODE"));
        promptLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        promptLabel.setVerticalAlignment(SwingConstants.CENTER);

        credentialPanel.add(promptLabel);

        codeField.setMaximumSize(codeField.getPreferredSize());
        codeField.addActionListener(evt -> {
            code = codeField.getText();
            setVisible(false);
        });
        credentialPanel.add(codeField);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(messagePanel);
        centerPanel.add(getOpenButtonPanel(initUrl));
        centerPanel.add(getEditorPane(BundleMessage.format("UI_0365_AUTHENTICATION_CODE_PROMPT")));
        centerPanel.add(credentialPanel);
        centerPanel.add(Box.createVerticalGlue());

        //add(messagePanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(getSendButtonPanel(), BorderLayout.SOUTH);
        setModal(true);

        pack();
        // center frame
        setLocation(getToolkit().getScreenSize().width / 2 -
                        getSize().width / 2,
                getToolkit().getScreenSize().height / 2 -
                        getSize().height / 2);
        setAlwaysOnTop(true);
        setVisible(true);
    }

    private JEditorPane getEditorPane(String text) {
        JEditorPane jEditorPane = new JEditorPane();
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        StyleSheet stylesheet = htmlEditorKit.getStyleSheet();
        Font font = jEditorPane.getFont();
        stylesheet.addRule("body { font-size:small;font-family: " + ((font==null)?"Arial":font.getFamily()) + '}');
        jEditorPane.setEditorKit(htmlEditorKit);
        jEditorPane.setContentType("text/html");
        jEditorPane.setText(text);

        jEditorPane.setEditable(false);
        jEditorPane.setOpaque(false);
        jEditorPane.addHyperlinkListener(hle -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                try {
                    DesktopBrowser.browse(hle.getURL().toURI());
                } catch (URISyntaxException e) {
                    DavGatewayTray.error(new BundleMessage("LOG_UNABLE_TO_OPEN_LINK"), e);
                }
            }
        });
        return jEditorPane;
    }

    protected JPanel getOpenButtonPanel(final String initUrl) {
        JPanel buttonPanel = new JPanel();
        JButton openButton = new JButton(BundleMessage.format("UI_BUTTON_OPEN"));
        JButton copyButton = new JButton(BundleMessage.format("UI_BUTTON_COPY"));
        openButton.addActionListener(evt -> DesktopBrowser.browse(initUrl));
        copyButton.addActionListener(evt -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(initUrl), null);
        });

        buttonPanel.add(openButton);
        buttonPanel.add(copyButton);
        return buttonPanel;
    }

    protected JPanel getSendButtonPanel() {
        JPanel buttonPanel = new JPanel();
        JButton sendButton = new JButton(BundleMessage.format("UI_BUTTON_SEND"));
        JButton cancelButton = new JButton(BundleMessage.format("UI_BUTTON_CANCEL"));
        sendButton.addActionListener(evt -> {
            code = codeField.getText();
            setVisible(false);
        });
        cancelButton.addActionListener(evt -> {
            code = null;
            setVisible(false);
        });

        buttonPanel.add(sendButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }
}
