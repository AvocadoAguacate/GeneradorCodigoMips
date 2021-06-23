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
    private int ultimoTemporalAsignado;
    
    public Generador(String source){
        
        String temporales_int[] = {"","","","","","","",""};
        temporalesInt = temporales_int;
        ultimoTemporalAsignado = -1;
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
        for(int i = 0; i < source.length; i++){
            if(source[i].contains("=")){ //asignaciones
                text += "#" + source[i] + "\n";
                asignaciones(source[i],i);
            } else if (source[i].contains(":") && !source[i].contains("#") && !source[i].contains("\"")){ //etiqueta
                text += source[i] + "\n";
            } else if (source[i].startsWith("#")){ //comentario
                text += source[i] + "\n";
            } else if (source[i].contains("return")){
                returnCase(source[i]);
            } else if (source[i].contains("write")){
                text += "#" + source[i] + "\n";
                writeCase(source[i]);
            } else if(source[i].contains("if ")){
                text += "#" + source[i] + "\n";
                ifCase(source[i]);
            } else if(source[i].contains("goto ")){
                goCase(source[i]);
            } else if(source[i].contains("goback")){
                text += "jr $ra";
            } else if(source[i].contains("call ")){
                callCase(source[i]);
            }
        }
    }
    
    private void callCase(String linea){
        String destino = linea.replace("call ", "");
        text += "jal "+ destino + "\n";
    }
    
    private void goCase(String linea){
        linea = linea.replace("goto ", "").replace(" ", "");
        text += "j " + linea + "\n";
    }
    
    private void ifCase(String linea){
        linea = linea.replace("if ", "");
        String[] lineaIf = linea.split("goto");
        String condicion = lineaIf[0].replace(" ", "").replace("Int_", "");
        String destino = lineaIf[1].replace(" ", "");
        int indexTemp = getPosTempOrPila(condicion); //aqui revisa si está en un temporal o si está en pila y lo carga
        String condicionMips = "$t" + indexTemp;
        text+= "beq " + condicionMips + ", 1, " + destino + "\n";
    }
    
    private void asignaciones(String linea,int numeroLinea){
        String[] split = linea.split("=",2);
        String p1 = split[0];
        if(p1.contains("String_")){
            asignacionString(p1,split[1]);
        } else if (p1.contains("Int_")){
            asignacionInt(p1,split[1],numeroLinea);
        } 
    }
    
    private void asignacionString(String p1,String p2){
        String id = p1.replace("String_", "").replace(" ", "");
        data += id + " : .asciiz " + p2 + "\n"; 
    }
    
    private void asignacionInt(String p1, String p2, int numeroLinea){
        String id = p1.replace("Int_", "").replace(" ", "");
        if(p2.contains("+")){
            sumaCase(id,p2);
        } else if(p2.contains("-")){
            restaCase(id,p2);
        } else if(p2.contains(">") || p2.contains("<") ||
                p2.contains("==") || p2.contains("&") ||
                p2.contains("|")){
            comparacionesCase(id,p2,numeroLinea);
        } else {
            String suma = p2 + " + 0";
            sumaCase(id,suma);
        }
    }
    
    private void paramCase(String id, String p2){
        
    }
    
    private void liCase(String id, String p2){
        String p2Clear = p2.replaceAll(" ", "");
        String idMips = "$t" + getTemporalInt(id);
        text += "li "+ idMips + ", " + p2Clear + "\n"; 
    }
    
    private void comparacionesCase(String id, String p2, int numeroLinea){
        if(p2.contains(">")){
           String[] operandos = p2.split(">");
           String op1 = operandos[0].replace(" ", ""); 
           String op2 = operandos[1].replace(" ", "");
           String operando1 = getOperando(op1);
           String operando2 = getOperando(op2);
           int temporalInt = getTemporalInt(id);
           text += "slt $t"+ temporalInt + "," + operando2 + "," + operando1 + "\n"; 
        } else if(p2.contains("<")){
           String[] operandos = p2.split("\\<");
           String operando1 = getOperando(operandos[0].replace(" ", ""));
           String operando2 = getOperando(operandos[1].replace(" ", ""));
           int temporalInt = getTemporalInt(id);
           text += "slt $t"+ temporalInt + "," + operando1 + "," + operando2 + "\n"; 
        } else if(p2.contains("==")){
           System.out.println("Aun no está ==");
        } else if(p2.contains("&")){
           String[] operandos = p2.split("\\&");
           String operando1 = getOperando(operandos[0].replace(" ", ""));
           String operando2 = getOperando(operandos[1].replace(" ", ""));
           int temporalInt = getTemporalInt(id);
           text += "and $t"+ temporalInt + "," + operando1 + "," + operando2 + "\n";
        } else if(p2.contains("|")){
           String[] operandos = p2.split("\\|");
           String operando1 = getOperando(operandos[0].replace(" ", ""));
           String operando2 = getOperando(operandos[1].replace(" ", ""));
           int temporalInt = getTemporalInt(id);
           text += "or $t"+ temporalInt + "," + operando1 + "," + operando2 + "\n";
        }
    }
    
    private void sumaCase(String id,String p2){
        String[] operandos = p2.split("\\+");
        String operando1 = getOperando(operandos[0].replace(" ", ""));
        String operando2 = getOperando(operandos[1].replace(" ", ""));
        String add = "addi";
        if(operando2.contains("$t")){
            add = "add";
        }
        int temporalInt = getTemporalInt(id);
        text += add + " $t"+ temporalInt +"," + operando1 + "," + operando2 + "\n"; 
    }
    
    private void restaCase(String id,String p2){
        String[] operandos = p2.split("\\-");
        String operando1 = getOperando(operandos[0].replace(" ", ""));
        String operando2 = getOperando(operandos[1].replace(" ", ""));
        String sub = "subi";
        if(operando2.contains("$t")){
            sub = "sub";
        }
        int temporalInt = getTemporalInt(id);
        text += "subi $t"+ temporalInt +"," + operando1 + "," + operando2 + "\n"; 
    }
    
    private void returnCase(String linea){
        if(linea.contains("return 0")){
            text += "# return 0\n";
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
        } else if(linea.contains("Int_")) {
            String id = linea.replace("write", "").replace("(", "").replace(")", "")
                    .replace("Int_", "");
            int tempPos = getPosTempOrPila(id);
            String idMips = "$" + tempPos;
            text += "li $v0, 1\n"
                    + "move $a0, " + idMips + 
                    "\nsyscall\n";
        }
    }
    
    private int getTemporalInt(String id){
        int resultado = -1;
        for(int i = 0; i < temporalesInt.length; i++){
            if(temporalesInt[i].length() == 0){
                temporalesInt[i] = id;
                resultado = i;
                ultimoTemporalAsignado = i;
                return resultado;
            }
        }
        if(ultimoTemporalAsignado == 7){
            ultimoTemporalAsignado = -1;
        }
        for(int x = 0; x < temporalesInt.length;x++){
            if(!temporalesInt[x].contains("Temp_") && ultimoTemporalAsignado < x){
               //escribimos el mips
               text += "# liberando el temporal $t" + x + ", y guardando en pila a " + temporalesInt[x] + "\n";
               text += "sub $sp, $sp, 4\n"; //ajustamos el puntero sp
               text += "sw $t"+ x + ", 0 ($sp)\n"; //guardar en pila a $tx
               //guardamos en la pila en java la información necesaria
               addToken(temporalesInt[x],4);
               temporalesInt[x] = id;
               //retornamos el espacio liberado
               resultado = x;
               ultimoTemporalAsignado = x;
               return  resultado;
            }
        }
        for(int j = 0; j < temporalesInt.length;j++){
            if(temporalesInt[j].contains("Temp_") && ultimoTemporalAsignado < j){
                temporalesInt[j] = id;
                resultado = j;
                ultimoTemporalAsignado = j;
                return resultado;
            }
        }
        if ( resultado == -1){ //en teoria no creo que caiga aquí
            resultado = tempIntLleno;
            temporalesInt[tempIntLleno] = id;
            tempIntLleno += 1;
            if(tempIntLleno == 8){
                tempIntLleno = 0;
            }
        }
        return resultado;
    }
    
    private int getPosTempInt(String id){
        System.out.println("getPosTempInt");
        System.out.println(id);
        System.out.println(tempToString());
        id = id.replaceAll(" ", "");
        for (int i = 0; i < temporalesInt.length; i++){
            System.out.println(id);
            System.out.println(temporalesInt[i]);
            System.out.println("("+id+")("+temporalesInt[i]+")");
            System.out.println(temporalesInt[i].equals(id));
            if(temporalesInt[i].equals(id)){
                System.out.println("return");
                System.out.println(i);
                return i;
            }
        }
        return -1;
    }
    
    /**
     * La idea es calcular el numero relativo a la posición de SP
     * lw   $t0, # ($sp) donde # es el numero relativo deseado
     * @param id el nombre de la variable en la pila para obtener su posición en la pila
     * @return  el numero relativo deseado
     */
    private int getSpRelativo(String id){
        int SpActual = pila.get(pila.size()-1).getPosicion();
        int SpDeseado = getToken(id).getPosicion();
        return SpActual - SpDeseado;
    }
    
    private String getOperando(String id){
        if(id.contains("Int_")){
            id = id.replace("Int_", "");
            int indexTemp = getPosTempOrPila(id); //aqui revisa si está en un temporal o si está en pila y lo carga
            return "$t" + indexTemp;
        }
        return id;
    }
    
    private int getPosTempOrPila(String id){
        //buscar en temporales
        int tempIndex = getPosTempInt(id);
        if(tempIndex > -1){
            return tempIndex;
        } else {
            //hay que cargar desde la pila
            int indexTemporal = getTemporalInt(id); //en la función se asigna el id al temporal cedido
            //escribir el mips
            text += "# Cargar desde la pila a " + id + " en el temporal $t" + 
                    indexTemporal + "\n";
            text += "lw $t" + indexTemporal + ", " + getSpRelativo(id) + "($sp)\n";
            return indexTemporal;
        }
    }

    @Override
    public String toString() {
        return "Generador{" + "\nsource=" + toStringSource() + ",\npila=" + pila + '}';
    }
    public String tempToString(){
        String result = "";
        for(String id:temporalesInt){
            result += id + ",";
        }
        return result;
    }
    
}
