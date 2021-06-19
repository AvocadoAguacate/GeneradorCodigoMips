/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GeneradorMips;

import java.util.ArrayList;

/**
 *
 * @author Esteban Guzmán R
 */
public class Generador {
    private String[] source;
    private ArrayList<Token> pila;
    
    public Generador(String source){
        this.source = source.split("\n");
        pila = new ArrayList<Token>();
    }
    
    

    /**
     * Retorna el godigo mips
     * @return codigo mips
     */
    String getCodigoMips() {
        String result = "No hay nada todavía";
        return result; 
    }
    
    /**
     * Buscar un token
     * @param id id del token
     * @return  token buscado o null si no existe en la pila
     */
    Token getToken(String id){
        for (Token token : pila){
            if(token.getId().compareTo(id) == 0){
                return token;
            }
        }
        return null;
    }
    
    /**
     * Agregar un token en la pila
     * @param token token por agregar
     */
    void addToken(String id, int size){
        if(getToken(id) == null){
            Token temp = new Token(id,size,getSiguientePosicion());
            pila.add(temp);
        }
    }
    
    /**
     * Revisa cual sería la siguiente posición en la pila, en caso de estar 
     * vacía entonces le daría la primera posición (0). 
     * @return la siguiente posición
     */
    int getSiguientePosicion(){
        if(pila.size() > 0){
            Token lastTemp = pila.get(pila.size()-1);
            return lastTemp.getPosicion()+lastTemp.getSize();
        } else {
            return 0;
        }
        
    }
    
}
