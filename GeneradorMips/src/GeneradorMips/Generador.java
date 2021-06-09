/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GeneradorMips;

/**
 *
 * @author Esteban Guzmán R
 */
public class Generador {
    private String[] source;
    
    public Generador(String source){
        this.source = source.split(";");
    }
}
