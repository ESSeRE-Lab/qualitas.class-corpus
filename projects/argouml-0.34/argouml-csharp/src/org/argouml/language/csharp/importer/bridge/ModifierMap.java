package org.argouml.language.csharp.importer.bridge;

/**
 * Class to map modifiers between parser and modeller
 * @author Thilina Hasantha <thilina.hasantha@gmail.com>
 */
public class ModifierMap {

    public static short getUmlModifierForVisibility(long mod){
        short smod=0x0000;
        if((mod & 0x0000002) >0){
            smod += 0x0001;
        }
        else if((mod & 0x0000010)>0){
            smod += 0x0002;
        }
        else if((mod & 0x0000004)>0){
            smod += 0x0004;
        }
        else if((mod &0x0000100)>0){
            smod += 0x0008;
        }
        else if((mod & 0x0000040)>0){
            smod += 0x0010;
        }
        else{
            smod += 0x0000;
        }

        return smod;

    }
}
