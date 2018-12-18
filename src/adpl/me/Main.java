package adpl.me;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class Main {
    static Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");
        int generacionesLimite = 1000, nIndividuos_estacionario, nIndividuos_generacional, iteraccionesBusqLocal;
        double prob_generacional = 0.7, prob_mutacion = 0.001;
        long tStart, tEnd;
        boolean estacionario = false, generacional = false;

        if ( args.length < 1 ) {
            System.out.println("No se han proporcionado parámetros de ejecución,");
            System.out.println("Generando fichero para ejecución automática");

            Properties configuracion = inicializarPropiedades();

            String[] archivos = {" "};
            String[] algoritmos = {};
            String[] cargaSemillas = {};
            String[] cargaIteracciones = {};
            String[] cargaNIndividuos = {};

            generacionesLimite = Integer.parseInt(configuracion.getProperty("limite_generaciones"));
            nIndividuos_estacionario = Integer.parseInt(configuracion.getProperty("poblacion_estacionario"));
            nIndividuos_generacional = Integer.parseInt(configuracion.getProperty("poblacion_generacional"));


            String archivosEjecucion = configuracion.getProperty("input");
            String algoritmosEjecucion = configuracion.getProperty("algoritmo");
            String semillasEjecucion = configuracion.getProperty("semillas");
            String iteraccionesEjecucion = configuracion.getProperty("iteracciones_busq_local");
            String nIndividuosBusqEjecucion = configuracion.getProperty("nIndividuos_busq_local");

            prob_generacional = Double.parseDouble(configuracion.getProperty("probabilidad_cruce_generacional"));
            prob_mutacion = Double.parseDouble(configuracion.getProperty("probabilidad_de_mutacion"));

            archivos = archivosEjecucion.split(",");
            algoritmos = algoritmosEjecucion.split(",");
            cargaSemillas = semillasEjecucion.split(",");
            cargaIteracciones = iteraccionesEjecucion.split(",");
            cargaNIndividuos = nIndividuosBusqEjecucion.split(",");

            int c = 0;

            int nSemillas = cargaSemillas.length;
            int semillas[] = new int[nSemillas];
            while (c < nSemillas) {
                semillas[c] = Integer.parseInt(cargaSemillas[c]);
                c++;
            }

            c = 0;
            int nIteracciones = cargaIteracciones.length;
            int iteracciones[] = new int[nIteracciones];
            while ( c < nIteracciones ) {
                iteracciones[c] = Integer.parseInt(cargaIteracciones[c]);
                c++;
            }

            c = 0;
            int nIndividuosBusq = cargaNIndividuos.length;
            int nIndividuosBusLocal[] = new int[nIndividuosBusq];
            while ( c < nIndividuosBusq ) {
                nIndividuosBusLocal[c] = Integer.parseInt(cargaNIndividuos[c]);
                c++;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter("ejecucion.bat"));

            for (String algoritmo : algoritmos) {
                if ("estacionario".equals(algoritmo.toString())) {
                    for (String archivo : archivos) {
                        for (int semilla : semillas) {
                            for (int ite : iteracciones) {
                                System.out.println("java -jar Metaheuristicas_Practica3.jar " + archivo + " " + semilla + " est 25 1000 100 " + ite);
                                writer.write("java -jar Metaheuristicas_Practica3.jar " + archivo + " " + semilla + " est 25 1000 100 " + ite + "\n");
                            }
                        }
                    }
                }
                if ("generacional".equals(algoritmo.toString())) {
                    for (String archivo : archivos) {
                        for (int semilla : semillas) {
                            for (int pob : nIndividuosBusLocal) {
                                for (int ite : iteracciones) {
                                    System.out.println("java -jar Metaheuristicas_Practica3.jar " + archivo + " " + semilla + " gen 10 1000 " + pob + " " + ite);
                                    writer.write("java -jar Metaheuristicas_Practica3.jar " + archivo + " " + semilla + " gen 10 1000 " + pob + " " + ite + "\n");
                                }
                            }
                        }
                    }
                }
            }
            writer.close();
        } else {
            logger.setUseParentHandlers(false);

            Handler fileHandler = new FileHandler();
            fileHandler = new FileHandler("./results/ultima_ejecucion.log");
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);

            logger.addHandler(fileHandler);

            logger.log(Level.INFO, "Parámetros:");
            logger.log(Level.INFO, "Fichero: " + args[0]);
            logger.log(Level.INFO, "Semilla: " + args[1]);
            logger.log(Level.INFO, "Algoritmo: " + args[2]);
            logger.log(Level.INFO, "Tam. Población: " + args[3]);
            logger.log(Level.INFO, "Cond. Generaciones: " + args[4]);
            logger.log(Level.INFO, "Busq Local % Población: " + args[5]);
            logger.log(Level.INFO, "Busq Local Iteraciones: " + args[6]);

            fileHandler.close();

            List<Individuo> poblacion = new ArrayList<>();

            String archivo = args[0];
            int semilla = Integer.parseInt(args[1]);
            String algoritmo = args[2];
            int nIndividuos = Integer.parseInt(args[3]);
            int generaciones = Integer.parseInt(args[4]);
            int nIndividuosBusqLocal = Integer.parseInt(args[5]);
            int nIteraccionesBusqLocal = Integer.parseInt(args[6]);

            String contenido = readFile(archivo);

            int f[][] = dataGestFrecuencias(contenido);
            int d[][] = dataGestLocalizaciones(contenido);

            if ( "est".equals(algoritmo) ) {
                fileHandler = new FileHandler("./results/" + archivo.substring(0, 5) + "_estacionario_" + semilla + "_est_25_" + generaciones + "_100_" + nIteraccionesBusqLocal + ".log");
                fileHandler.setFormatter(simpleFormatter);
                logger.addHandler(fileHandler);
                logger.log(Level.INFO, "Iniciando ejecución, fichero: " + archivo);

                System.out.println("Ejecutando versión estacionaria | Almacenando en fichero: " + archivo.substring(0, 5) + "_estacionario_" + semilla + "_est_25_" + generaciones + "_100_" + nIteraccionesBusqLocal + ".log");
                for (int i = 0; i < nIndividuos; i++) {
                    poblacion.add(new Individuo(f.length, semilla));
                    poblacion.get(i).evaluar(f, d);
                }
                logger.log(Level.INFO, " | VERSIÓN: ESTACIONARIA | PMX OFF");
                tStart = System.currentTimeMillis();
                algoritmoGeneticoEstacionario(poblacion, false, prob_mutacion, generacionesLimite, f, d, nIteraccionesBusqLocal, semilla);
                tEnd = System.currentTimeMillis();
                logger.log(Level.INFO, "The task has taken " + (tEnd - tStart) + " milliseconds.");
                poblacion.clear();

                fileHandler.close();
            }

            if ( "gen".equals(algoritmo) ) {
                fileHandler = new FileHandler("./results/" + archivo.substring(0, 5) + "_generacional_" + semilla + "_gen_10_" + generaciones + "_" + nIndividuosBusqLocal + "_" + nIteraccionesBusqLocal + ".log");
                fileHandler.setFormatter(simpleFormatter);
                logger.addHandler(fileHandler);
                logger.log(Level.INFO, "Iniciando ejecución, fichero: " + archivo + " | Semilla = " + semilla);

                System.out.println("Ejecutando versión generacional | Almacenando en fichero: " + archivo.substring(0, 5) + "_generacional_" + semilla + "_gen_10_" + generaciones + "_" + nIndividuosBusqLocal + "_" + nIteraccionesBusqLocal + ".log");
                logger.log(Level.INFO, " | VERSIÓN: GENERACIONAL | PMX ON");
                for (int i = 0; i < nIndividuos; i++) {
                    poblacion.add(new Individuo(f.length, semilla));
                    poblacion.get(i).evaluar(f, d);
                }
                tStart = System.currentTimeMillis();
                algoritmoGeneticoGeneracional(poblacion, true, prob_generacional, prob_mutacion, generacionesLimite, f, d, nIteraccionesBusqLocal, nIndividuosBusqLocal, semilla);
                tEnd = System.currentTimeMillis();
                logger.log(Level.INFO, "The task has taken " + (tEnd - tStart) + " milliseconds.");
            }
        }
    }

    /**
     *
     * @param poblacion      Población inicial generada aleatoriamente
     * @param PMX            Ejecutar PMX o En Orden, PMX si la variable es true, En Orden si PMX es false
     * @param prob_cruce     Probabilidad para realizar un cruce
     * @param prob_mutacion  Probabilidad para realizar mutación de gen
     * @param stop           Condición de parada
     * @param f              Matriz de frecuencias
     * @param d              Matriz de distancias
     * @param semilla        Semilla para la ejecución
     */
    public static void algoritmoGeneticoGeneracional(List<Individuo> poblacion, boolean PMX, double prob_cruce, double prob_mutacion, int stop, int[][] f, int[][] d, int busqStop, int busqPoblacion, int semilla) {
        Individuo mejor = obtenerMejorIndividuo(poblacion);
        Individuo elite = obtenerMejorIndividuo(poblacion);
        Individuo nuevoMejor;
        Individuo aux;
        int tamPoblacion = poblacion.size();
        int t = 0;
        int mejorGeneracion = 0;
        Random rnd = new Random();
        rnd.setSeed(semilla);
        double ejCruce;

        List<Individuo> ganadoresTorneo = new ArrayList<>();
        List<Individuo> hijos = new ArrayList<>();

        do {
            t++;
            ganadoresTorneo.clear();
            ////////////////////////////////////////////
            //// PROCESO DE SELECCIÓN DE INDIVIDUOS ////
            ////         POR TORNEO, K = 2          ////
            ////////////////////////////////////////////

            for ( int i = 0; i < tamPoblacion; i++ ) {
                ganadoresTorneo.add(seleccionPorTorneo(poblacion, semilla));
            }

            ////////////////////////////////////////////
            ////   RECOMBINACIÓN DE DESCENDIANTES   ////
            ////    MEDIANTE CRUCE EN ORDEN Y PMX   ////
            ////////////////////////////////////////////

            ejCruce = rnd.nextDouble();

            hijos.clear();
            for ( int i = 0; i < tamPoblacion-1; i+=2 ) {
                if ( PMX ) {
                    if ( ejCruce < prob_cruce ) {
                        hijos.add(crucePMX(ganadoresTorneo.get(i), ganadoresTorneo.get(i + 1), semilla));
                        hijos.add(crucePMX(ganadoresTorneo.get(i + 1), ganadoresTorneo.get(i), semilla));
                    } else {
                        hijos.add(ganadoresTorneo.get(i));
                        hijos.add(ganadoresTorneo.get(i+1));
                    }
                } else {
                    if ( ejCruce < prob_cruce ) {
                        hijos.add(cruceEnOrden(ganadoresTorneo.get(i), ganadoresTorneo.get(i + 1), semilla));
                        hijos.add(cruceEnOrden(ganadoresTorneo.get(i + 1), ganadoresTorneo.get(i), semilla));
                    } else {
                        hijos.add(ganadoresTorneo.get(i));
                        hijos.add(ganadoresTorneo.get(i+1));
                    }
                }
            }

            poblacion.clear();
            for ( int i = 0; i < tamPoblacion; i++ ) {
                ////////////////////////////////////////////
                ////   MUTACIÓN DE LOS DESCENCIENTES    ////
                ////      OBTENIDOS EN LOS CRUCES       ////
                ////////////////////////////////////////////

                hijos.get(i).evaluar(f, d);
                hijos.get(i).mutacion(prob_mutacion, f, d);
            }

            if ( t % 10 == 0 ) {
                if ( busqPoblacion == 10 ) {
                    for ( int i = 0; i < tamPoblacion; i++ ) {
                        int[] dlb = new int[hijos.get(i).getGenotipo().length];
                        hijos.get(i).setGenotipo(calculoPrimerMejor(hijos.get(i), dlb, f, d, busqStop));
                        hijos.get(i).evaluar(f, d);
                        for (int j = 0; j < dlb.length; j++) {
                            dlb[j] = 0;
                        }
                    }
                } else if ( busqPoblacion == 3 ) { //FixME: Falla este método
                    int posMejor1 = 0;
                    int posMejor2 = hijos.size()-1;
                    int posMejor3 = 1;
                    Individuo mejores[] = new Individuo[3];
                    Individuo mejor1, mejor2, mejor3;
                    mejor1 = hijos.get(0);
                    mejor2 = hijos.get(hijos.size()-1);
                    mejor3 = hijos.get(1);

                    for ( int i = 0; i < hijos.size()-1; i++ ) {
                        if ( mejor1.getValor() > hijos.get(i).getValor() && mejor1.getValor() != mejor2.getValor() && mejor1.getValor() != mejor3.getValor()  ) {
                            mejor1 = hijos.get(i);
                            posMejor1 = i;
                        }
                    }

                    for ( int i = 1; i < hijos.size(); i++ ) {
                        if ( mejor2.getValor() > hijos.get(i).getValor() && mejor1.getValor() != mejor2.getValor() && mejor2.getValor() != mejor3.getValor() ) {
                            mejor2 = hijos.get(i);
                            posMejor2 = i;
                        }
                    }

                    for ( int i = 1; i < hijos.size(); i++ ) {
                        if ( mejor3.getValor() > hijos.get(i).getValor() && mejor3.getValor() != mejor1.getValor() && mejor3.getValor() != mejor2.getValor() ) {
                            mejor3 = hijos.get(i);
                            posMejor3 = i;
                        }
                    }

                    for ( int j = 0; j < mejores.length; j++ ) {
                        int[] dlb = new int[mejores[j].getGenotipo().length];
                        mejores[j].setGenotipo(calculoPrimerMejor(mejores[j], dlb, f, d, busqStop));
                        for (int k = 0; j < dlb.length; k++) {
                            dlb[k] = -1;
                        }
                    }

                    for ( Individuo elmejor : mejores )
                        elmejor.evaluar(f, d);
                } else if ( busqPoblacion == 1 ) {
                    Individuo randomBusqLocal = hijos.get(rnd.nextInt(tamPoblacion));
                    int[] dlb = new int[randomBusqLocal.getGenotipo().length];
                    randomBusqLocal.setGenotipo(calculoPrimerMejor(randomBusqLocal, dlb, f, d, busqStop));
                    randomBusqLocal.evaluar(f, d);
                }
            }

            for ( int i = 0; i < tamPoblacion; i++ ) {
                poblacion.add(hijos.get(i));
            }

                //////////////////////////////////////////////
                ////   EVALUACIÓN DE LOS DESCENCIENTES    ////
                ////      OBTENIDOS TRAS MUTAR GENES      ////
                //////////////////////////////////////////////

            if ( !poblacion.contains(elite) ) {
                Individuo peor = obtenerPeorIndividuo(poblacion);
                if ( elite.getValor() < peor.getValor() ) {
                    peor.setId(elite.getId());
                    peor.setGenotipo(elite.getGenotipo());
                    peor.setValor(elite.getValor());
                }
            }

            nuevoMejor = obtenerMejorIndividuo(poblacion);
            if ( nuevoMejor.getValor() < mejor.getValor() ) {
                logger.log(Level.INFO, "CAMBIO DE ÉLITE: " + elite.toString());
                logger.log(Level.INFO, mejor.toString() + " ha sido el mejor durante " + mejorGeneracion + " generaciones. | DESDE LA " + (t-mejorGeneracion) + " HASTA LA " + (t) + "." );
                mejor.setId(nuevoMejor.getId());
                mejor.setGenotipo(nuevoMejor.getGenotipo());
                mejor.setValor(nuevoMejor.getValor());
                mejorGeneracion = 0;
            } else {
                mejorGeneracion++;
            }

        } while ( t < stop );

        logger.log(Level.INFO, mejor.toString() + " ha sido el mejor durante " + mejorGeneracion + " generaciones. | DESDE LA " + (t-mejorGeneracion+1) + " HASTA LA " + (t+1) + "." );
        logger.log(Level.INFO, "Así el mejor final es: " + mejor.toString() + " | obtenido en la generación " + (t-mejorGeneracion+1));

    }

    /**
     *
     * @param poblacion      Población inicial generada aleatoriamente
     * @param PMX            Ejecutar PMX o En Orden, PMX si la variable es true, En Orden si PMX es false
     * @param prob_mutacion  Probabilidad para realizar mutación de gen
     * @param stop           Condición de parada
     * @param f              Matriz de frecuencias
     * @param d              Matriz de distancias
     * @param semilla        Semilla para la ejecución
     */
    public static void algoritmoGeneticoEstacionario(List<Individuo> poblacion, boolean PMX, double prob_mutacion, int stop, int[][] f, int[][] d, int busqStop, int semilla) {
        Individuo mejor = poblacion.get(0), nuevoMejor;
        int t = 0, mejorGeneracion = 0;
        List<Individuo> ganadoresTorneo = new ArrayList<>();
        Random rnd = new Random();
        rnd.setSeed(semilla);
        double ejCruce;

        do {
            ganadoresTorneo.clear();
            t++;

            ////////////////////////////////////////////
            //// PROCESO DE SELECCIÓN DE INDIVIDUOS ////
            ////         POR TORNEO, K = 2          ////
            ////////////////////////////////////////////

            ganadoresTorneo.add(seleccionPorTorneo(poblacion, semilla));
            ganadoresTorneo.add(seleccionPorTorneo(poblacion, semilla));

            ////////////////////////////////////////////
            ////   RECOMBINACIÓN DE DESCENDIANTES   ////
            ////    MEDIANTE CRUCE EN ORDEN Y PMX   ////
            ////////////////////////////////////////////

            ejCruce = rnd.nextDouble();
            Individuo hijo1, hijo2;

            if ( PMX ) {
                hijo1 = crucePMX(ganadoresTorneo.get(0), ganadoresTorneo.get(1), semilla);
                hijo2 = crucePMX(ganadoresTorneo.get(1), ganadoresTorneo.get(0), semilla);
            } else {
                hijo1 = cruceEnOrden(ganadoresTorneo.get(0), ganadoresTorneo.get(1), semilla);
                hijo2 = cruceEnOrden(ganadoresTorneo.get(1), ganadoresTorneo.get(0), semilla);
            }

            ////////////////////////////////////////////
            ////   MUTACIÓN DE LOS DESCENCIENTES    ////
            ////      OBTENIDOS EN LOS CRUCES       ////
            ////////////////////////////////////////////
            hijo1.evaluar(f, d);
            hijo2.evaluar(f, d);

            hijo1.mutacion(prob_mutacion, f, d);
            hijo2.mutacion(prob_mutacion, f, d);

            //////////////////////////////////////////////
            ////   EVALUACIÓN DE LOS DESCENCIENTES    ////
            ////      OBTENIDOS TRAS MUTAR GENES      ////
            //////////////////////////////////////////////

            if ( t % 50 == 0 ) {
                int[] dlb1 = new int[hijo1.getGenotipo().length];
                hijo1.setGenotipo(calculoPrimerMejor(hijo1, dlb1, f, d, busqStop));
                int[] dlb2 = new int[hijo2.getGenotipo().length];
                hijo2.setGenotipo(calculoPrimerMejor(hijo1, dlb2, f, d, busqStop));
            }

            ////////////////////////////////////////////
            ////  COMPARACIÓN DE LOS DESCENCIENTES  ////
            ////      PARA SABER QUIEN TIENE        ////
            ////    MÁS PRIORIDAD PARA SUBSISTIR    ////
            ////////////////////////////////////////////

            Individuo mejores[] = new Individuo[2];

            if ( hijo1.getValor() < hijo2.getValor() ) {
                mejores[0] = hijo1;
                mejores[1] = hijo2;
            } else {
                mejores[0] = hijo2;
                mejores[1] = hijo1;
            }

            ////////////////////////////////////////////
            ////   SELECCIÓN DE LOS PEORES INDIV    ////
            ////   DE LA POBLACIÓN SIN TENER EN     ////
            ////      CUENTA LOS DESCENDIENTES      ////
            ////////////////////////////////////////////

            Individuo peor1, peor2;
            peor1 = poblacion.get(0);
            peor2 = poblacion.get(poblacion.size()-1);

            int posPeor1 = 0;
            int posPeor2 = poblacion.size()-1;
            Individuo peores[] = new Individuo[2];

            for ( int i = 0; i < poblacion.size()-1; i++ ) {
                if ( peor1.getValor() < poblacion.get(i).getValor() && peor1.getValor() != peor2.getValor() ) {
                    peor1 = poblacion.get(i);
                    posPeor1 = i;
                }
            }

            for ( int i = 1; i < poblacion.size(); i++ ) {
                if ( peor2.getValor() < poblacion.get(i).getValor() && peor1.getValor() != peor2.getValor() ) {
                    peor2 = poblacion.get(i);
                    posPeor2 = i;
                }
            }

            ////////////////////////////////////////////
            ////   COMPARACIÓN Y REEMPLAZAMIENTO    ////
            ////   DE INDIVIDUOS DESCENDIENTES Y    ////
            ////     PEORES, SE REEMPLAZA SII       ////
            ////           SON MEJORES              ////
            ////////////////////////////////////////////

            if ( peor1.getValor() < peor2.getValor() ) {
                if ( mejores[0].getValor() < peor2.getValor() ) poblacion.set(posPeor2, mejores[0]);
                if ( mejores[1].getValor() < peor1.getValor() ) poblacion.set(posPeor1, mejores[1]);
            } else {
                if ( mejores[0].getValor() < peor1.getValor() ) poblacion.set(posPeor1, mejores[0]);
                if ( mejores[1].getValor() < peor2.getValor() ) poblacion.set(posPeor2, mejores[1]);
            }

            ////////////////////////////////////////////////
            ////   GESTIÓN DE INFORMACIÓN PARA EL LOG   ////
            ////////////////////////////////////////////////

            nuevoMejor = obtenerMejorIndividuo(poblacion);
            if ( nuevoMejor.getValor() < mejor.getValor() ) {
                logger.log(Level.INFO, mejor.toString() + " ha sido el mejor durante " + mejorGeneracion + " generaciones. \t| DESDE LA " + (t-mejorGeneracion) + " HASTA LA " + (t) + "." );
                mejor.setId(nuevoMejor.getId());
                mejor.setGenotipo(nuevoMejor.getGenotipo());
                mejor.setValor(nuevoMejor.getValor());
                mejorGeneracion = 0;
            } else {
                mejorGeneracion++;
            }
        } while ( t < stop );

        logger.log(Level.INFO, mejor.toString() + " ha sido el mejor durante " + mejorGeneracion + " generaciones. | DESDE LA " + (t-mejorGeneracion+1) + " HASTA LA " + (t+1) + "." );
        logger.log(Level.INFO, "Así el mejor final es: " + mejor.toString() + " | obtenido en la generación " + (t-mejorGeneracion+1));
    }

    /**
     * Obtiene el mejor individuo de una población
     * @param poblacion Conjunto de individuos donde buscamos el mejor
     * @return Mejor individuo de la población
     */
    public static Individuo obtenerMejorIndividuo(List<Individuo> poblacion) {
        Individuo mejor = poblacion.get(0);
        for ( Individuo i : poblacion ) {
            if ( i.getValor() < mejor.getValor() )
                mejor = i;
        }
        return mejor;
    }

    /**
     * Obtiene el peor individuo de una población
     * @param poblacion Conjunto de individuos donde buscamos el peor
     * @return Peor individuo de la población
     */
    public static Individuo obtenerPeorIndividuo(List<Individuo> poblacion) {
        Individuo peor = poblacion.get(0);
        for ( Individuo i : poblacion ) {
            if ( i.getValor() > peor.getValor() )
                peor = i;
        }
        return peor;
    }

    /**
     * Cruza dos individuos usando cruce en Orden
     * @param padre1 Primer individuo
     * @param padre2 Segundo individuo
     * @param semilla Semilla para la generación de randoms
     * @return Individuo cruzado
     */
    public static Individuo cruceEnOrden(Individuo padre1, Individuo padre2, int semilla) {
        Random r = new Random();
        r.setSeed(semilla);
        int tamGenotipo = padre1.getGenotipo().length;
        Individuo hijo = new Individuo(tamGenotipo, semilla);
        int[] genotipoPadre1 = padre1.getGenotipo();
        int[] genotipoPadre2 = padre2.getGenotipo();
        int[] genotipoHijo = new int[tamGenotipo];
        boolean insertado = false;
        int corte1, corte2;

        do {
            corte1 = r.nextInt(tamGenotipo);
            corte2 = r.nextInt(tamGenotipo)+1;
        } while ( corte1 >= corte2 );

        for ( int i = 0; i < genotipoHijo.length; i++ ) {
            genotipoHijo[i] = -1;
        }

        for ( int i = corte1; i < corte2; i++ ) {
            genotipoHijo[i] = genotipoPadre1[i];
        }

        for ( int i = 0; i < genotipoPadre2.length; i++ ) {
            for ( int j = 0; j < genotipoHijo.length; j++ ) {
                if ( genotipoPadre2[i] == genotipoHijo[j] ) {
                    insertado = true;
                }
            }
            if ( !insertado ) {
                for ( int k = corte2; k < genotipoHijo.length; k++ ) {
                    if ( genotipoHijo[k] == -1 ) {
                        genotipoHijo[k] = genotipoPadre2[i];
                        break;
                    }
                }
            }
            insertado = false;
        }

        for ( int i = 0; i < genotipoPadre2.length; i++ ) {
            for ( int j = 0; j < genotipoHijo.length; j++ ) {
                if ( genotipoPadre2[i] == genotipoHijo[j] ) {
                    insertado = true;
                }
            }
            if ( !insertado ) {
                for ( int k = 0; k < corte1; k++ ) {
                    if ( genotipoHijo[k] == -1 ) {
                        genotipoHijo[k] = genotipoPadre2[i];
                        break;
                    }
                }
            }
            insertado = false;
        }

        hijo.setGenotipo(genotipoHijo);
        return hijo;
    }

    /**
     * Cruza dos individuos usando cruce PMX
     * @param padre1 Primer individuo
     * @param padre2 Segundo individuo
     * @param semilla Semilla para la generación de randoms
     * @return Individuo cruzado
     */
    public static Individuo crucePMX(Individuo padre1, Individuo padre2, int semilla) {
        Random r = new Random();
        r.setSeed(semilla);
        int tamGenotipo = padre1.getGenotipo().length;
        Individuo hijo = new Individuo(tamGenotipo, semilla);
        int[] genotipoPadre1 = padre1.getGenotipo();
        int[] genotipoPadre2 = padre2.getGenotipo();
        int[] genotipoHijo = new int[padre1.getGenotipo().length];
        int corte1, corte2;
        boolean insertado = false;

        do {
            corte1 = r.nextInt(tamGenotipo);
            corte2 = r.nextInt(tamGenotipo)+1;
        } while ( corte1 >= corte2 );

        for ( int i = 0; i < genotipoHijo.length; i++ ) {
            genotipoHijo[i] = -1;
        }

        int iniciosPadre1[] = new int[(corte2-corte1)+1];
        for ( int i = 0; i < iniciosPadre1.length-1; i++ ) {
            iniciosPadre1[i] = padre1.getGenotipo()[corte1+i];
        }

        for ( int i = corte1; i < corte2; i++ ) {
            genotipoHijo[i] = genotipoPadre2[i];
        }

        for ( int n : iniciosPadre1 ) {
            boolean continuar = false;
            boolean encontrado = false;
            int busq = n;
            int busqAux = n;
            int posicion = corte1;
            int posicion2 = corte1;

            do {
                for ( int i = 0; i < genotipoHijo.length; i++ ) {
                    if ( n == genotipoHijo[i] ) {
                        encontrado = true;
                        continuar = false;
                        break;
                    }
                }

                if ( !encontrado ) {
                    for ( int i = 0; i < genotipoPadre1.length; i++ ) {
                        if ( busq == genotipoPadre1[i] ) {
                            posicion = i;
                            busqAux = genotipoPadre2[i];
                            break;
                        }
                    }

                    for (int i = 0; i < genotipoPadre1.length; i++) {
                        if (busqAux == genotipoPadre1[i]) {
                            if ( genotipoHijo[i] == -1) {
                                genotipoHijo[i] = n;
                                continuar = false;
                                break;
                            } else {
                                busq = busqAux;
                                continuar = true;
                                break;
                            }
                        }
                    }
                }
            } while ( continuar );
            hijo.setGenotipo(genotipoHijo);
        }

        for ( int i = 0; i < genotipoPadre2.length; i++ ) {
            for ( int j = 0; j < genotipoHijo.length; j++ ) {
                if ( genotipoPadre2[i] == genotipoHijo[j] ) {
                    insertado = true;
                }
            }
            if ( !insertado ) {
                for ( int k = corte2; k < genotipoHijo.length; k++ ) {
                    if ( genotipoHijo[k] == -1 ) {
                        genotipoHijo[k] = genotipoPadre2[i];
                        break;
                    }
                }
            }
            insertado = false;
        }

        for ( int i = 0; i < genotipoPadre2.length; i++ ) {
            for ( int j = 0; j < genotipoHijo.length; j++ ) {
                if ( genotipoPadre2[i] == genotipoHijo[j] ) {
                    insertado = true;
                }
            }
            if ( !insertado ) {
                for ( int k = 0; k < corte1; k++ ) {
                    if ( genotipoHijo[k] == -1 ) {
                        genotipoHijo[k] = genotipoPadre2[i];
                        break;
                    }
                }
            }
            insertado = false;
        }

        hijo.setGenotipo(genotipoHijo);

        return hijo;
    }

    /**
     * Selecciona el mejor individuo entre 2 individuos generados al azar
     * @param poblacion Conjunto de individuos
     * @param semilla Semilla para la generación de randoms
     * @return Mejor individuo de los dos enfrentados
     */
    public static Individuo seleccionPorTorneo(List<Individuo> poblacion, int semilla) {
        Random r = new Random();
        r.setSeed(semilla);
        int individuo1, individuo2;

        do {
            individuo1 = r.nextInt(poblacion.size());
            individuo2 = r.nextInt(poblacion.size());
        } while ( individuo1 == individuo2);

        return ( poblacion.get(individuo1).getValor() < poblacion.get(individuo2).getValor() ) ? poblacion.get(individuo1) : poblacion.get(individuo2);
    }

    /**
     * Algoritmo de búsqueda local del primer mejor
     * @param u Vector de unidades
     * @param dlb Vector de Don't Look Bit
     * @param f Matriz de frecuencias
     * @param d Matriz de distancias
     * @return Permutación de unidades encontrada más eficiente
     */
    public static int[] calculoPrimerMejor(Individuo individuo, int[] dlb, int[][] f, int[][] d, int stop) {
        int eval = 0;
        int coste = 0;
        int comprobadas = 0;
        int diferencia;
        boolean improve_flag = false;
        int solucion[] = new int[individuo.getGenotipo().length];
        System.arraycopy(individuo.getGenotipo(), 0, solucion, 0, individuo.getGenotipo().length);

        do {
            comprobadas = 0;
            for (int i = 0; i < solucion.length; i++) {
                if (dlb[i] == 0) {
                    improve_flag = false;
                    for (int j = 0; j < solucion.length; j++) {
                        diferencia = checkMove(i, j, solucion, f, d);
                        if ( diferencia < 0 ) {
                            applyMove(i, j, solucion);
                            dlb[i] = dlb[j] = 0;
                            improve_flag = true;
                        }
                    }
                    if (!improve_flag) dlb[i] = 1;
                } else {
                    comprobadas++;
                }
            }
            eval++;
        } while ( comprobadas < individuo.getGenotipo().length && eval < stop );

        coste = calcularTotalPermutacion(f, d, solucion);
        return solucion;
    }

    /**
     * Cálcula la diferencia de costes en caso de aplicar un movimiento
     * entre las unidades recibidas
     * @param i Primera unidad
     * @param j Segunda unidad
     * @param u Vector de unidades
     * @param f Matriz de frecuencias
     * @param d Matriz de distancias
     * @param coste Coste de la solución actual
     * @return True si reduce el coste, false en cualquier otro caso
     */
    public static int checkMove(int i, int j, int[] u, int[][] f, int[][] d) {
        int k;
        int suma = 0;
        for ( k = 0; k < u.length; k++ ) {
            if (i !=j && k != i && k != j) {
                suma += f[j][k] * (d[u[i]][u[k]] - d[u[j]][u[k]])
                        + f[i][k] * (d[u[j]][u[k]] - d[u[i]][u[k]])
                        + f[k][j] * (d[u[k]][u[i]] - d[u[k]][u[j]])
                        + f[k][i] * (d[u[k]][u[j]] - d[u[k]][u[i]]);
            }
        }
        return suma;
    }

    /**
     * Aplica intercambio de posiciones de las unidades
     * @param i Primera posición
     * @param j Segunda posición
     * @param u Vector de unidades
     */
    public static void applyMove(int i, int j, int[] u) {
        int aux = u[i];
        u[i] = u[j];
        u[j] = aux;
    }

    public static int calcularTotalPermutacion(int[][] f, int[][] d, int[] permutacion) {
        int suma = 0;

        for (int i = 0; i < f.length; i++) {
            for (int j = 0; j < d.length; j++) {
                if ( i != j ) {
                    suma += f[i][j] * d[permutacion[j]][permutacion[i]];
                }
            }
        }

        return suma;
    }


    public static Properties inicializarPropiedades() {
        try {
            Properties configuracion = new Properties();

            configuracion.load(new FileInputStream("main.properties"));

            return configuracion;
        } catch (IOException e) {
            System.out.println("Error al leer el fichero");
            return null;
        }
    }

    public static void writeFile(String archivo) throws IOException {

    }

    public static String readFile(String archivo) throws IOException {
        String result = "", line;
        FileReader f = new FileReader(archivo);
        BufferedReader br = new BufferedReader(f);
        while((line = br.readLine()) != null) {
            result = result.concat(line + "\n");
        }
        br.close();
        return result;
    }

    public static int[][] dataGestFrecuencias(String text) throws IOException {
        String[] numbers;
        numbers = text.split("\\s+");

        int tam = Integer.parseInt(numbers[0]);
        int frecuencias[][] = new int[tam][tam];

        int i = 1, j = 0, k = 0;

        do {
            frecuencias[k][j] = Integer.parseInt(numbers[i]);
            j++;

            if ( j - tam == 0 ) {
                j = 0;
                k++;
            }

            i++;
        } while ( i < (tam*tam) );

        return frecuencias;
    }

    public static int[][] dataGestLocalizaciones(String text) throws IOException {
        String[] numbers;
        numbers = text.split("\\s+");

        int tam = Integer.parseInt(numbers[0]);
        int localizaciones[][] = new int[tam][tam];

        int i = 1, j = 0, k = 0;

        do {
            localizaciones[k][j] = Integer.parseInt(numbers[i+(tam*tam)]);
            j++;

            if ( j - tam == 0 ) {
                j = 0;
                k++;
            }

            i++;
        } while ( i < (tam*tam) );

        return localizaciones;
    }
}