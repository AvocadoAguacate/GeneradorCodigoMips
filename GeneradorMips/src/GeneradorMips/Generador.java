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
    private String[] temporalesFloat;
    private ArrayList<Token> pila;
    private String data;
    private String text;
    private int tempIntLleno; 
    private int tempFloatLleno;
    private int ultimoTemporalAsignadoInt;
    private int ultimoTemporalAsignadoFloat;
    
    public Generador(String source){
        
        String temporales_int[] = {"","","","","","","",""};
        String temporales_float[] = {"","","","","","","",""};
        temporalesInt = temporales_int;
        temporalesFloat = temporales_float;
        ultimoTemporalAsignadoInt = -1;
        ultimoTemporalAsignadoFloat = -1;
        tempIntLleno = 0;
        tempFloatLleno = 0;
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
        for (int i = pila.size()-1; i > -1;i--){
            if(pila.get(i).getId().compareTo(id) == 0){
                return pila.get(i);
            }
        }
        return null;
    }
    
    /**
     * Agregar un token en la pila
     * @param token token por agregar
     */
    public void addToken(String id, int size){ //cambiar a private al final
        Token temp = new Token(id,size,getSiguientePosicion());
        pila.add(temp);
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
    
    /**
     * Se encarga de una llamada de función, encuentra el destino y lo escribe
     * @param linea la linea a traducir
     */
    private void callCase(String linea){
        String destino = linea.replace("call ", "");
        text += "jal "+ destino + "\n";
    }
    
    /**
     * Se encarga de los casos goto
     * @param linea la linea a traducir
     */
    private void goCase(String linea){
        linea = linea.replace("goto ", "").replace(" ", "");
        text += "j " + linea + "\n";
    }
    
    /**
     * Se encarga del caso if
     * @param linea linea if a traducir
     */
    private void ifCase(String linea){
        linea = linea.replace("if ", "");
        String[] lineaIf = linea.split("goto");
        String condicion = lineaIf[0].replace(" ", "").replace("Int_", "");
        String destino = lineaIf[1].replace(" ", "");
        int indexTemp = getPosTempOrPila(condicion,true); //aqui revisa si está en un temporal o si está en pila y lo carga
        String condicionMips = "$t" + indexTemp;
        text+= "beq " + condicionMips + ", 1, " + destino + "\n";
    }
    
    /**
     * Se encarga de todas las lineas que contengan un =
     * @param linea linea a traducir
     * @param numeroLinea numero de linea 
     */
    private void asignaciones(String linea,int numeroLinea){
        String[] split = linea.split("=",2);
        String p1 = split[0];
        if(p1.contains("String_")){
            asignacionString(p1,split[1]);
        } else if (p1.contains("Int_")){
            asignacionInt(p1,split[1],numeroLinea);
        } else if (p1.contains("Float_")){
            asignacionesFloat(p1,split[1],numeroLinea);
        }
    }
    
    /**
     * Se encarga de las asignaciones tipo float
     * @param p1 sería el p1 en p1 = op1 op op2
     * @param p2 p2 = op1 op op2 en p1 = op1 op op2
     * @param numeroLinea numero de linea
     */
    private void asignacionesFloat(String p1, String p2, int numeroLinea) {
        String id = p1.replace("Float_", "").replace(" ", "");
        if(p2.contains("+")){
            sumaCase(id,p2,false);
        } else if(p2.contains("-")){
            restaCase(id,p2,false);
        } else if(p2.contains("/")){
            divisionCase(id,p2,false);
        } else if(p2.contains("*")){
            multiplicacionCase(id,p2,false);
        } else {
            liCase(id,p2,false);
        }
    }
    
    /**
     * Guarda los string en data
     * @param p1 id del string
     * @param p2 string a guardar
     */
    private void asignacionString(String p1,String p2){
        String id = p1.replace("String_", "").replace(" ", "");
        data += id + " : .asciiz " + p2 + "\n"; 
    }
    
    /**
     * Encargado de las asignaciones de enteros
     * @param p1 sería el p1 en p1 = op1 op op2
     * @param p2 p2 = op1 op op2 en p1 = op1 op op2
     * @param numeroLinea numero de linea
     */
    private void asignacionInt(String p1, String p2, int numeroLinea){
        String id = p1.replace("Int_", "").replace(" ", "");
        if(p2.contains("+")){
            sumaCase(id,p2,true);
        } else if(p2.contains("-")){
            restaCase(id,p2,true);
        } else if(p2.contains(">") || p2.contains("<") ||
                p2.contains("==") || p2.contains("&") ||
                p2.contains("|")){
            comparacionesCase(id,p2,numeroLinea);
        } else if(p2.contains("/")){
            divisionCase(id,p2,true);
        } else if(p2.contains("*")){
            multiplicacionCase(id,p2,true);
        } else if(p2.contains("~")){
            modCase(id,p2);
        } else if(p2.contains("read()")){
            readCase(id,p2,false);
        }else {
            liCase(id,p2,true);
        }
    }
    
    private void readCase(String id,String p2,boolean entero){
        if(entero){
            //Se solicita el input en mips
            text += "#Se solicita el entero\n";
            text += "li $v0, 5\nsyscall\n";
            //Se solicita el temporal en java
            String idMips = "$t" + getTemporalInt(id);
            //Se copia el input en un registro 
            text += "#Se copia el read() en " + idMips + "\n";
            text += "move " + idMips + ",$v0\n";
        } else { //flotante
            //reviso si $f0 esta ocupado
            if(temporalesFloat[0].length() > 0 && !temporalesFloat[0].contains("Temp_")){
               text += "#liberando el temporal $f0, y guardando en pila a " + temporalesFloat[0] + "\n";
               text += "sub $sp, $sp, 4\n"; //ajustamos el puntero sp
               text += "s.s $f0, 0 ($sp)\n"; //guardar en pila a $tx
               //guardamos en la pila en java la información necesaria
               addToken(temporalesFloat[0],4); 
            }
            //Se solicita el input en mips
            text += "#Se solicita el flotante\n";
            text += "li $v0, 6\nsyscall\n";
            //Se solicita el temporal en java
            String idMips = "$f" + getTemporalFloat(id);
            text += "#Se copia el read() en " + idMips + "\n";
            text += "mov.s " + idMips + ",$f0\n";
        }
    }
    
    /**
     * Se encarga de la operación residuo
     * @param id sería el p1 en p1 = op1 op op2 
     * @param p2 p2 = op1 op op2 en p1 = op1 ~ op2
     */
    private void modCase(String id,String p2){
        String idMips = "$t" + getTemporalInt(id);
        String[] operandos = p2.split("~");
        String operando1 = getOperando(operandos[0].replace(" ", ""));
        String operando2 = getOperando(operandos[1].replace(" ", ""));
        text += "div " + operando1 + ", " + operando2 + "\n";
        text += "mfhi " + idMips + "\n";
    }
    
    /**
     * Se encarga de la operacion multiplicacion
     * @param id sería el p1 en p1 = op1 op op2 
     * @param p2 p2 = op1 op op2 en p1 = op1 * op2
     */
    private void multiplicacionCase(String id, String p2,boolean entero) {
        String[] operandos = p2.split("\\*");
        String operando1 = getOperando(operandos[0].replace(" ", ""));
        String operando2 = getOperando(operandos[1].replace(" ", ""));
        if(entero){
            String idMips = "$t" + getTemporalInt(id);
            text += "mult " + operando1 + ", " + operando2 + "\n";
            text += "mflo " + idMips + "\n";
        } else { //floatante
            String idMips = "$f" + getTemporalInt(id);
            text += "mul.s " + idMips + "," + operando1 + "," + operando2 + "\n";
        }
        
    }
    
    /**
     * Se encarga de la operacion división
     * @param id sería el p1 en p1 = op1 op op2 
     * @param p2 p2 = op1 op op2 en p1 = op1 / op2
     */
    private void divisionCase(String id,String p2,boolean entero){
        
        String[] operandos = p2.split("/");
        String operando1 = getOperando(operandos[0].replace(" ", ""));
        String operando2 = getOperando(operandos[1].replace(" ", ""));
        if(entero){
            String idMips = "$t" + getTemporalInt(id);
            text += "div " + operando1 + ", " + operando2 + "\n";
            text += "mflo " + idMips + "\n";
        } else {
            String idMips = "$f" + getTemporalFloat(id);
            text += "div.s "+ idMips + "," + operando1 + "," + operando2 + "\n";
        }
        
    }
    
    /**
     * Se encarga de la operacion = sencilla
     * @param id sería el p1 en p1 = op1 
     * @param p2 p2 = op1 en p1 = op1
     * @param entero indica si el caso es entero (true) o flotante (false)
     */
    private void liCase(String id, String p2, boolean entero){
        //Encontrar el temporal de su tipo
        String idMips = "";
        if(entero){
            idMips = "$t" + getTemporalInt(id); 
        } else {
            idMips = "$f" + getTemporalFloat(id);
        }
        
        String p2Clear = p2.replaceAll(" ", "");
        
        //Encontrar si es explicito o implicito
        if (p2.contains("Int_")) { // Int var = otraVar
            String operando1 = getOperando(p2Clear);
            text += "move " + idMips + "," + operando1 + "\n";
        } else if (p2.contains("Float_")){ // Float var = otraVar
            String operando1 = getOperando(p2Clear);
            text += "mov.s " + idMips + "," + operando1 + "\n";
        } else if(p2.contains(".")){ //var = float
            text += "li.s " + idMips + "," + p2Clear + "\n";
        } else { // var = integer
            text += "li " + idMips + "," + p2Clear + "\n";
        }
    }
    
    /**
     * Se encarga de la operacion comparación
     * @param id sería el p1 en p1 = op1 op op2 
     * @param p2 p2 = op1 op op2 en p1 = op1 op op2
     * @param numeroLinea linea de codigo
     */
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
    
    /**
     * Se encarga de la operacion suma
     * @param id sería el p1 en p1 = op1 + op2 
     * @param p2 p2 = op1 + op2 en p1 = op1 + op2
     */
    private void sumaCase(String id,String p2, boolean entero){
        String[] operandos = p2.split("\\+");
        String operando1 = getOperando(operandos[0].replace(" ", ""));
        String operando2 = getOperando(operandos[1].replace(" ", ""));
        if(entero){
            String add = "addi";
            if(operando2.contains("$t")){
                add = "add";
            }
            int temporalInt = getTemporalInt(id);
            text += add + " $t"+ temporalInt + "," + operando1 + "," + operando2 
                    + "\n"; 
        } else { //flotante
            int temporalFloat = getTemporalFloat(id);
            text += "add.s $f" + temporalFloat + "," + operando1 + "," + operando2 
                    + "\n";
        }
        
    }
    
    /**
     * Se encarga de la operacion resta
     * @param id sería el p1 en p1 = op1 - op2 
     * @param p2 p2 = op1 - op2 en p1 = op1 - op2
     */
    private void restaCase(String id,String p2,boolean entero){
        String[] operandos = p2.split("\\-");
        String operando1 = getOperando(operandos[0].replace(" ", ""));
        String operando2 = getOperando(operandos[1].replace(" ", ""));
        if (entero) {
            String sub = "subi";
            if (operando2.contains("$t")) {
                sub = "sub";
            }
            int temporalInt = getTemporalInt(id);
            text += "subi $t" + temporalInt + "," + operando1 + "," + operando2 
                    + "\n";
        } else { //flotante
            int temporalFloat = getTemporalFloat(id);
            text += "sub.s $f" + temporalFloat + "," + operando1 + "," + operando2 
                    + "\n";
        }
        
    }
    
    /**
     * Se encarga de los casos return, actualmente solo lee el caso return 0 ( fin de main ) 
     * @param linea 
     */
    private void returnCase(String linea){
        if(linea.contains("return 0")){
            text += "#return 0\n";
            text += "li $v0, 10\nsyscall";
        }
    }
    
    /**
     * Print
     * @param linea linea de codigo a traducir
     */
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
            int tempPos = getPosTempOrPila(id,true);
            String idMips = "$t" + tempPos;
            text += "li $v0, 1\n"
                    + "move $a0, " + idMips + 
                    "\nsyscall\n";
        } else if(linea.contains("Float_")){
            String id = linea.replace("write", "").replace("(", "").replace(")", "")
                    .replace("Float_", "");
            int tempPos = getPosTempOrPila(id,false);
            String idMips = "$f" + tempPos;
            text += "li $v0, 2\n"
                    + "mov.s $f12, " + idMips + 
                    "\nsyscall\n";
        }
    }
    
    /**
     * Encargado de buscar temporales y asignar variables a los mismos. En caso
     * de ser necesario libera registros guardando en pila 
     * @param id el nuevo registro
     * @return numero del registro 
     */
    private int getTemporalInt(String id){
        int resultado = -1;
        //Recorrido para buscar temporales vacios
        for(int i = 0; i < temporalesInt.length; i++){
            if(temporalesInt[i].length() == 0){
                temporalesInt[i] = id;
                resultado = i;
                ultimoTemporalAsignadoInt = i;
                return resultado;
            }
        }
        if(ultimoTemporalAsignadoInt == 7){
            ultimoTemporalAsignadoInt = -1;
        }
        //Recorrido para buscar temporales con variables guardables
        for(int x = 0; x < temporalesInt.length;x++){
            if(!temporalesInt[x].contains("Temp_") && ultimoTemporalAsignadoInt < x){
               //escribimos el mips
               text += "#liberando el temporal $t" + x + ", y guardando en pila a " + temporalesInt[x] + "\n";
               text += "sub $sp, $sp, 4\n"; //ajustamos el puntero sp
               text += "sw $t"+ x + ", 0 ($sp)\n"; //guardar en pila a $tx
               //guardamos en la pila en java la información necesaria
               addToken(temporalesInt[x],4);
               temporalesInt[x] = id;
               //retornamos el espacio liberado
               resultado = x;
               ultimoTemporalAsignadoInt = x;
               return  resultado;
            }
        }
        //Recorrido para buscar temporales con temporales
        for(int j = 0; j < temporalesInt.length;j++){
            if(temporalesInt[j].contains("Temp_") && ultimoTemporalAsignadoInt < j){
                temporalesInt[j] = id;
                resultado = j;
                ultimoTemporalAsignadoInt = j;
                return resultado;
            }
        }
        //Caso extremo, no debería llegar aquí
        if ( resultado == -1){
            resultado = tempIntLleno;
            temporalesInt[tempIntLleno] = id;
            tempIntLleno += 1;
            if(tempIntLleno == 8){
                tempIntLleno = 0;
            }
        }
        return resultado;
    }
    
     /**
     * Encargado de buscar temporales y asignar variables a los mismos. En caso
     * de ser necesario libera registros guardando en pila 
     * @param id el nuevo registro
     * @return numero del registro 
     */
    private int getTemporalFloat(String id) {
        int resultado = -1;
        //Recorrido para buscar temporales vacios
        for(int i = 0; i < temporalesFloat.length; i++){
            if(temporalesFloat[i].length() == 0){
                temporalesFloat[i] = id;
                resultado = i;
                ultimoTemporalAsignadoFloat = i;
                return resultado;
            }
        }
        if(ultimoTemporalAsignadoFloat == 7){
            ultimoTemporalAsignadoFloat = -1;
        }
        //Recorrido para buscar temporales con variables guardables
        for(int x = 0; x < temporalesFloat.length;x++){
            if(!temporalesFloat[x].contains("Temp_") && ultimoTemporalAsignadoFloat < x){
               //escribimos el mips
               text += "#liberando el temporal $f" + x + ", y guardando en pila a " + temporalesFloat[x] + "\n";
               text += "sub $sp, $sp, 4\n"; //ajustamos el puntero sp
               text += "s.s $f"+ x + ", 0 ($sp)\n"; //guardar en pila a $tx
               //guardamos en la pila en java la información necesaria
               addToken(temporalesFloat[x],4);
               temporalesFloat[x] = id;
               //retornamos el espacio liberado
               resultado = x;
               ultimoTemporalAsignadoFloat = x;
               return  resultado;
            }
        }
        //Recorrido para buscar temporales con temporales
        for(int j = 0; j < temporalesFloat.length;j++){
            if(temporalesFloat[j].contains("Temp_") && ultimoTemporalAsignadoFloat < j){
                temporalesFloat[j] = id;
                resultado = j;
                ultimoTemporalAsignadoFloat = j;
                return resultado;
            }
        }
        //Caso extremo, no debería llegar aquí
        if ( resultado == -1){
            resultado = tempFloatLleno;
            temporalesFloat[tempFloatLleno] = id;
            tempFloatLleno += 1;
            if(tempFloatLleno == 8){
                tempFloatLleno = 0;
            }
        }
        return resultado;
    }
    
    /**
     * Busca una variable en los temporales
     * @param id id de la variable a buscar
     * @return numero de registro buscado
     */
    private int getPosTempInt(String id){
        System.out.println("getPosTempInt");
        System.out.println(id);
        System.out.println(tempToString());
        id = id.replaceAll(" ", "");
        for (int i = temporalesInt.length-1; i > -1; i--){
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
     * Busca una variable en los temporales
     * @param id id de la variable a buscar
     * @return numero de registro buscado
     */
    private int getPosTempFloat(String id) {
        id = id.replaceAll(" ", "");
        for (int i = temporalesFloat.length-1; i > -1; i--){
            if(temporalesFloat[i].equals(id)){
                return i;
            }
        }
        return -1;
    }
    
    /**
     * La idea es calcular el numero relativo a la posición de SP
     * lw   $t0, #($sp) donde #es el numero relativo deseado
     * @param id el nombre de la variable en la pila para obtener su posición en la pila
     * @return  el numero relativo deseado
     */
    private int getSpRelativo(String id){
        int SpActual = pila.get(pila.size()-1).getPosicion();
        int SpDeseado = getToken(id).getPosicion();
        return SpActual - SpDeseado;
    }
    
    /**
     * Se encargada de los operandos, retorna el registro necesario 
     * @param id el imput del operando, puede ser un entero o flotante explicito 
     * @return el registro del operando, $t# o $f#
     */
    private String getOperando(String id){
        if(id.contains("Int_")){
            id = id.replace("Int_", "");
            int indexTemp = getPosTempOrPila(id,true); //aqui revisa si está en un temporal o si está en pila y lo carga
            return "$t" + indexTemp;
        } else if(id.contains("Float_")){
            int indexTemp = getPosTempOrPila(id,false);
            return "$f" + indexTemp;
        } else if(id.contains(".")){
            //Guardando en el generador
            id = id.replace(" ", "");
            int indexTemp = getTemporalFloat(id);
            String mipsTemp = "$f" + indexTemp;
            //Guardando en mips
            text += "#Se carga un temporal para " + id + " en $f" + indexTemp + "\n";
            text += "li.s " + mipsTemp + "," + id + "\n";
            return mipsTemp;
        } else { // entero explicito
            //Guardando en el generador
            id = id.replace(" ", "");
            int indexTemp = getTemporalInt(id);
            String mipsTemp = "$t" + indexTemp;
            //Guardando en mips
            text += "#Se carga un temporal para " + id + " en $t" + indexTemp + "\n";
            text += "li " + mipsTemp + "," + id + "\n";
            return mipsTemp;
        }
    }
    
    /**
     * Se encarga de revisar un id en los temporales o la pila, en caso de pila 
     * se encarga de cargarlo en temporal
     * @param id id de la variable a buscar
     * @param entero indica si es entero (true) o flotante(false)
     * @return numero del temporal donde ahora se encuentra la variable
     */
    private int getPosTempOrPila(String id, boolean entero){
        //buscar en temporales
        int tempIndex = -1;
        if(entero){
            tempIndex = getPosTempInt(id);
        } else {
            tempIndex = getPosTempFloat(id);
        }
        //devolver temporal en caso de que exista
        if(tempIndex > -1){
            return tempIndex;
        } else { //buscar en pila 
            //hay que cargar desde la pila
            int indexTemporal = -1;
            if (entero) {
                indexTemporal = getTemporalInt(id); //en la función se asigna el id al temporal cedido
                //escribir el mips
                text += "#Cargar desde la pila a " + id + " en el temporal $t"
                        + indexTemporal + "\n";
                int spRelativo = getSpRelativo(id);
                text += "lw $t" + indexTemporal + ", " + spRelativo + "($sp)\n";
            } else {
                //falta la parte de pila 
                indexTemporal = getTemporalFloat(id); //en la función se asigna el id al temporal cedido
                //escribir el mips
                text += "#Cargar desde la pila a " + id + " en el temporal $f"
                        + indexTemporal + "\n";
                int spRelativo = getSpRelativo(id);
                text += "l.s $f" + indexTemporal + ", " + spRelativo + "($sp)\n";
            }
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
