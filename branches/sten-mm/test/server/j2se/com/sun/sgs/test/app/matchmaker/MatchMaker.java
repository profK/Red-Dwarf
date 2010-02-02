/*
 Copyright (c) 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa
 Clara, California 95054, U.S.A. All rights reserved.
 
 Sun Microsystems, Inc. has intellectual property rights relating to
 technology embodied in the product that is described in this document.
 In particular, and without limitation, these intellectual property rights
 may include one or more of the U.S. patents listed at
 http://www.sun.com/patents and one or more additional patents or pending
 patent applications in the U.S. and in other countries.
 
 U.S. Government Rights - Commercial software. Government users are subject
 to the Sun Microsystems, Inc. standard license agreement and applicable
 provisions of the FAR and its supplements.
 
 This distribution may include materials developed by third parties.
 
 Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered
 trademarks of Sun Microsystems, Inc. in the U.S. and other countries.
 
 UNIX is a registered trademark in the U.S. and other countries, exclusively
 licensed through X/Open Company, Ltd.
 
 Products covered by and information contained in this service manual are
 controlled by U.S. Export Control laws and may be subject to the export
 or import laws in other countries. Nuclear, missile, chemical biological
 weapons or nuclear maritime end uses or end users, whether direct or
 indirect, are strictly prohibited. Export or reexport to countries subject
 to U.S. embargo or to entities identified on U.S. export exclusion lists,
 including, but not limited to, the denied persons and specially designated
 nationals lists is strictly prohibited.
 
 DOCUMENTATION IS PROVIDED "AS IS" AND ALL EXPRESS OR IMPLIED CONDITIONS,
 REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
 ARE DISCLAIMED, EXCEPT TO THE EXTENT THAT SUCH DISCLAIMERS ARE HELD TO BE
 LEGALLY INVALID.
 
 Copyright © 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 California 95054, Etats-Unis. Tous droits réservés.
 
 Sun Microsystems, Inc. détient les droits de propriété intellectuels
 relatifs à la technologie incorporée dans le produit qui est décrit dans
 ce document. En particulier, et ce sans limitation, ces droits de
 propriété intellectuelle peuvent inclure un ou plus des brevets américains
 listés à l'adresse http://www.sun.com/patents et un ou les brevets
 supplémentaires ou les applications de brevet en attente aux Etats -
 Unis et dans les autres pays.
 
 Cette distribution peut comprendre des composants développés par des
 tierces parties.
 
 Sun, Sun Microsystems, le logo Sun et Java sont des marques de fabrique
 ou des marques déposées de Sun Microsystems, Inc. aux Etats-Unis et dans
 d'autres pays.
 
 UNIX est une marque déposée aux Etats-Unis et dans d'autres pays et
 licenciée exlusivement par X/Open Company, Ltd.
 
 see above Les produits qui font l'objet de ce manuel d'entretien et les
 informations qu'il contient sont regis par la legislation americaine en
 matiere de controle des exportations et peuvent etre soumis au droit
 d'autres pays dans le domaine des exportations et importations.
 Les utilisations finales, ou utilisateurs finaux, pour des armes
 nucleaires, des missiles, des armes biologiques et chimiques ou du
 nucleaire maritime, directement ou indirectement, sont strictement
 interdites. Les exportations ou reexportations vers des pays sous embargo
 des Etats-Unis, ou vers des entites figurant sur les listes d'exclusion
 d'exportation americaines, y compris, mais de maniere non exclusive, la
 liste de personnes qui font objet d'un ordre de ne pas participer, d'une
 facon directe ou indirecte, aux exportations des produits ou des services
 qui sont regi par la legislation americaine en matiere de controle des
 exportations et la liste de ressortissants specifiquement designes, sont
 rigoureusement interdites.
 
 LA DOCUMENTATION EST FOURNIE "EN L'ETAT" ET TOUTES AUTRES CONDITIONS,
 DECLARATIONS ET GARANTIES EXPRESSES OU TACITES SONT FORMELLEMENT EXCLUES,
 DANS LA MESURE AUTORISEE PAR LA LOI APPLICABLE, Y COMPRIS NOTAMMENT TOUTE
 GARANTIE IMPLICITE RELATIVE A LA QUALITE MARCHANDE, A L'APTITUDE A UNE
 UTILISATION PARTICULIERE OU A L'ABSENCE DE CONTREFACON.
*/
package com.sun.sgs.test.app.matchmaker;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.impl.util.LoggerWrapper;

import com.sun.sgs.test.app.matchmaker.common.CommandProtocol;

/**
 * The main class for the Match Maker application. When users join, they are 
 * wrapped in a Player object and registered in the ManagedReference namespace 
 * under their user name.  Because of this, the application assumes that 
 * user names are unique (logging in multiple times as the same user will 
 * produce unpredictable results).  The sample passwords file that is bundled 
 * with the sample distribution has two users: "guest" and "guest2", both 
 * with passwords of "guest".
 */
public class MatchMaker implements AppListener, ManagedObject, Serializable {
    
    private static final LoggerWrapper logger =
        new LoggerWrapper(Logger.getLogger(MatchMaker.class.getName()));

    
    private static final long serialVersionUID = 1L;

    private ManagedReference folderRoot;


    /**
     * Creates the root of the Folder tree, creating Lobbies and
     * subfolders along the way.
     */
    private Folder createRootFolder() {
        logger.log(Level.FINE, "Creating Folder System");
        URL url = null;
        try {
            String root = System.getProperty("com.sun.sgs.MatchMaker.rootDir");

            url = new URL("file:///" + root + "/matchmaker_config.xml");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        ConfigParser parser = new ConfigParser(url);

        return parser.getFolderRoot();

    }

    /**
     * {@inheritDoc}
     */
    public ClientSessionListener loggedIn(ClientSession session) {
        logger.log(Level.INFO, "MatchMaker: ClientSession [" + session.getName() 
                   + "] joined");
        
        Player p = new Player(session, folderRoot);
        
        AppContext.getDataManager().setBinding(session.getName(), p);
        
        return p;
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(Properties props) {
        logger.log(Level.FINE, "MatchMaker: Starting up");   

        AppContext.getDataManager().setBinding("UsernameMap", 
                                     new ManagedMap<String, ClientSession>());        
        
        AppContext.getDataManager().setBinding("LobbyMap", 
                                new ManagedMap<String, ManagedReference>());

        AppContext.getDataManager().setBinding("GameRoomMap", 
                                    new ManagedMap<String, ManagedReference>());        
        
        folderRoot = 
            AppContext.getDataManager().createReference(createRootFolder());
        logger.log(Level.FINE, "Finished starting up");        
    }
    
}
