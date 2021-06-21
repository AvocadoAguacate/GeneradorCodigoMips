/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GeneradorMips;

/**
 *
 * @author esteb
 */
public class Token {
    
    private String id;
    private int size;
    private int posicion;

    public Token(String id, int size, int posicion) {
        this.id = id;
        this.size = size;
        this.posicion = posicion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }
    

    @Override
    public String toString() {
        return "{Token " + "id=" + id + ", size=" + size + ", posicion=" + posicion + "}\n";
    }
    
    
    
}
