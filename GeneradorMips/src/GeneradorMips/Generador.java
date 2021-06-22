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
    private String[] temporalesInt;
    private ArrayList<Token> pila;
    private String data;
    private String text;
    private int tempIntLleno; 
    
    public Generador(String source){
        
        String temporales_int[] = {"","","","","","","",""};
        temporalesInt = temporales_int;
        tempIntLleno = 0;
        this.source = source.split("\n");
        pila = new ArrayList<Token>();
        data = ".data\n";
        text = ".text\n.globl main\n";
        traducir();
    }
    
    

    /**
     * Retorna el godigo mips
     * @return codigo mips
     */
    public String getCodigoMips() {
        String result = data+text; 
        return result; 
    }
    
    /**
     * Buscar un token
     * @param id id del token
     * @return  token buscado o null si no existe en la pila
     */
    private Token getToken(String id){
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
    public void addToken(String id, int size){ //cambiar a private al final
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
    private int getSiguientePosicion(){
        if(pila.size() > 0){
            Token lastTemp = pila.get(pila.size()-1);
            return lastTemp.getPosicion()+lastTemp.getSize();
        } else {
            return 0;
        }
        
    }
    
    private String toStringSource(){
        String result = "";
        for (String element :source){
            result+= "["+ element + "]";
        }
        return result;
    }
    
    /**
     * Separa los casos de cada linea, no me gusta swich
     */
    private void traducir(){
        for(String linea:source){
            if(linea.contains("=")){ //asignaciones
                asignaciones(linea);
            } else if (linea.contains(":") && !linea.contains("#") && !linea.contains("\"")){ //etiqueta
                text += linea + "\n";
            } else if (linea.startsWith("#")){ //comentario
                text += linea + "\n";
            } else if (linea.contains("return")){
                returnCase(linea);
            } else if (linea.contains("write")){
                writeCase(linea);
            }
        }
    }
    
    private void asignaciones(String linea){
        String[] split = linea.split("=",2);
        String p1 = split[0];
        if(p1.contains("String_")){
            asignacionString(p1,split[1]);
        } else if (p1.contains("Int_")){
            asignacionInt(p1,split[1]);
        }
    }
    
    private void asignacionString(String p1,String p2){
        String id = p1.replace("String_", "").replace(" ", "");
        data += id + " : .asciiz " + p2 + "\n"; 
    }
    
    private void asignacionInt(String p1,String p2){
        String id = p1.replace("Int_", "").replace(" ", "");
        String[] operandos = p2.split("\\+");
        String operando1 = operandos[0].replace(" ", "");
        String operando2 = operandos[1].replace(" ", "");
        text += "add $t"+ getTemporalInt(id) +"," + operando1 + "," + operando2 + "\n"; 
    }
    
    private void returnCase(String linea){
        if(linea.contains("return 0")){
            text += "li $v0, 10\nsyscall";
        }
    }
    
    private void writeCase(String linea){
        if(linea.contains("String_")){ //caso de imprimir un string
            String id = linea.replace("write", "").replace("(", "").replace(")", "")
                    .replace("String_", "");
            text += "li $v0, 4\n"
                    + "la $a0, " + id + 
                    "\nsyscall\n";
        }
    }
    
    private int getTemporalInt(String id){
        int resultado = -1;
        for(int i = 0; i < temporalesInt.length; i++){
            if(temporalesInt[i].length() == 0){
                temporalesInt[i] = id;
                resultado = i;
                return resultado;
            }
        }
        for(int j = 0; j < temporalesInt.length;j++){
            if(temporalesInt[j].contains("Temp_")){
                temporalesInt[j] = id;
                resultado = j;
                return resultado;
            }
        }
        if ( resultado == -1){
            resultado = tempIntLleno;
            tempIntLleno += 1;
            if(tempIntLleno == 8){
                tempIntLleno = 0;
            }
        }
        return resultado;
    }

    @Override
    public String toString() {
        return "Generador{" + "\nsource=" + toStringSource() + ",\npila=" + pila + '}';
    }
    
}
