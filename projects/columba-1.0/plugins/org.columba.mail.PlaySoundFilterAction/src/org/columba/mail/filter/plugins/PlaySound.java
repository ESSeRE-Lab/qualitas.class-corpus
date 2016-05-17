//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.filter.plugins;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.URL;

import org.columba.core.io.DiskIO;


public class PlaySound {
    public static void play(String filename) {
        play(DiskIO.getResourceURL("org/columba/mail/sound/" + filename));
    }

    public static void play(URL url) {
        if (url != null) {
            SoundLoader loader = new SoundLoader(url);
            loader.setPriority(Thread.MIN_PRIORITY);
            loader.start();
        }
    }
}


class SoundLoader extends Thread {
    protected URL url;

    SoundLoader(URL url) {
        this.url = url;
    }

    public void run() {
        AudioClip audioClip = Applet.newAudioClip(url);
        audioClip.play();
    }
}
