package main;
import lpsolve.*;
import util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.alg.AllDirectedPaths;

/**
 * Represent an unmutable linnear programming model and its solution if it is feasible
 * 
 * */
public class LpModel{
	
	private static final int MAX_ATTEMPT_RANDOM_GRAPH = 5000;

	/** Amount of graph's vertexs */
	public final int AMOUNT_OF_VERTEX;
	
	/** Amount of required graph's path from node 0 to node AMOUNT_OF_VERTEX -1 */
	public final int AMOUNT_OF_PATH;
	
	/** Maximum Weight for an edge. The graph's edges will have weight from 1 to MAX_WEIGHT_EDGE inclusively */
	public final int MAX_WEIGHT_EDGE;
	
	/** Density of graph's edges. it will be taken into account for generate a random graph */
	public final int EDGE_DENSITY;
	
	/** The graph for current model */
	public final ListenableDirectedWeightedGraph<Vertex,Edge> graph;
	
	/** The linnear programming model entry graph */
	public final ListenableDirectedWeightedGraph<Vertex,Edge> resultGraph;
	
	/** The entry graph vertexs */
	public final Vertex[] vertexs;
	
	/** The entry graph edges */
	public final Edge[] edges;

	/** The entry graph metrics */
	public final Metrics graphMetrics;
	
	/** The result graph metrics */
	public final Metrics resultMetrics;


	private LpModel(
			int AMOUNT_OF_VERTEX, int AMOUNT_OF_PATH,int MAX_WEIGHT_EDGE, 
			int EDGE_DENSITY,ListenableDirectedWeightedGraph<Vertex,Edge> graph, 
			Vertex[] vertexs, Edge[] edges) throws IOException, LpSolveException
	{
		this.AMOUNT_OF_VERTEX = AMOUNT_OF_VERTEX;
		this.AMOUNT_OF_PATH   = AMOUNT_OF_PATH;
		this.MAX_WEIGHT_EDGE  = MAX_WEIGHT_EDGE; 
		this.EDGE_DENSITY     = EDGE_DENSITY;
		this.graph            = graph;
		this.vertexs          = vertexs;
		this.edges            = edges;
		
		 Pair<Integer,ListenableDirectedWeightedGraph<Vertex,Edge>> solution = solve();
		 this.resultGraph = solution.b;		 
		 this.graphMetrics =  new Metrics(graph, vertexs[0], vertexs[this.AMOUNT_OF_VERTEX-1]);
		 this.resultMetrics = new Metrics(resultGraph, vertexs[0], vertexs[this.AMOUNT_OF_VERTEX-1]);

	}
	
	
	/**
	 * Generate a default lp model
	 * @throws IOException 
	 * @throws LpSolveException 
	 * 
	 * */
	public static LpModel DefaultLPModel() throws IOException, LpSolveException
	{
		LpModel lpModel = new LpModel(
										Parameters.AMOUNT_OF_NODES, Parameters.AMOUNT_OF_REQUIRED_PATHS,
				  						Parameters.MAX_WEIGHT_EDGE, Parameters.EDGE_DENSITY, Parameters.DEFAULT_GRAPH, 
				  						Parameters.Vertexs, Parameters.Edges);		
		return lpModel;
	}
	
	/**
	 * Generate a default lp model
	 * @throws LpSolveException 
	 * 
	 * */
	public static LpModel LpModelFromGraph(
											int AMOUNT_OF_VERTEX, int AMOUNT_OF_PATH,int MAX_WEIGHT_EDGE, 
											int EDGE_DENSITY,ListenableDirectedWeightedGraph<Vertex,Edge> graph, 
											Vertex[] vertexs, Edge[] edges) 
											throws IOException, LpSolveException
	{
		LpModel lpModel = new LpModel(AMOUNT_OF_VERTEX, AMOUNT_OF_PATH,
									  MAX_WEIGHT_EDGE, EDGE_DENSITY, graph, 
									  vertexs, edges);	
		
		return lpModel;
	}
	
	public static LpModel generateLpModel(int amount_of_vertex,int edge_density,int amount_of_path,int max_weight_edge) throws IOException, LpSolveException{
		LinkedList<Vertex> listVertexs = new LinkedList<Vertex>();
		LinkedList<Edge> listEdges     = new LinkedList<Edge>();
		ListenableDirectedWeightedGraph<Vertex,Edge> graph = generateGraph(amount_of_vertex,edge_density,max_weight_edge,amount_of_path,listVertexs,listEdges);
		
		//check if random graph could be generated
		if(graph == null)
			return null;
		
		Vertex[] vertexs = new Vertex[amount_of_vertex];
		Edge[] edges = new Edge[listEdges.size()];
		vertexs = listVertexs.toArray(vertexs);
		edges = listEdges.toArray(edges);
		
		LpModel model = new LpModel(amount_of_vertex, amount_of_path, max_weight_edge, edge_density, graph, vertexs, edges);
		return model;
	}
	
	private static ListenableDirectedWeightedGraph<Vertex,Edge> generateGraph(int amountOfNodes, int density, int max_weight_edge,int amount_of_path,List<Vertex> outVertex, List<Edge> outEdges){
		int k;
		List<GraphPath<Vertex,Edge>> paths;
		ListenableDirectedWeightedGraph<Vertex,Edge> graph = null;
		
		for(k=0; k<MAX_ATTEMPT_RANDOM_GRAPH;k++){
			while(!outVertex.isEmpty())outVertex.remove(0);
			while(!outEdges.isEmpty())outEdges.remove(0);
			graph = new ListenableDirectedWeightedGraph<Vertex,Edge>(Edge.class);			
			
			for(int i=0; i<amountOfNodes; i++){
				Vertex v = new Vertex("V"+i);
				outVertex.add(v);
				graph.addVertex(v);
			}
			
			Random rn  = new Random();
			Random rnw = new Random();
			for(int i=0; i<amountOfNodes; i++)
				for(int j=0; j<amountOfNodes; j++){
					if(i != j && rn.nextFloat()*100 < density){
						Edge e = new Edge("E"+outEdges.size(), outVertex.get(i),outVertex.get(j),rnw.nextInt(max_weight_edge) + 1 ); 
						graph.addEdge(e.from,e.to,e);					
						graph.setEdgeWeight(e, e.weight);
						outEdges.add(e);
					}
				}
			
			paths = getAllSinglePath(graph,outVertex.get(0),outVertex.get(amountOfNodes - 1));
			if(paths.size() >= amount_of_path)
				break;
		}
		if(k<MAX_ATTEMPT_RANDOM_GRAPH){
			Parameters.report.writeln("The Graph has been generated");
			return graph;
		}else 
			return null;
	}
	
	public ListenableDirectedWeightedGraph<Vertex,Edge> generateGraphFromSolution(double[] solution){
		 ListenableDirectedWeightedGraph<Vertex,Edge> resultGraph = new ListenableDirectedWeightedGraph<Vertex,Edge>(Edge.class);		
		
		for(int i=0; i<vertexs.length; i++)
			resultGraph.addVertex(vertexs[i]);
		
		for(int i=0; i<edges.length; i++)
			if(solution[i] == 1.00 ){
				resultGraph.addEdge(edges[i].from,edges[i].to, edges[i]);
				resultGraph.setEdgeWeight(edges[i], edges[i].weight);
			}
		
		return resultGraph;
	}
	
	/**
	 * Solve current Linnear Programming model. 
	 * 
	 * */
	private Pair<Integer,ListenableDirectedWeightedGraph<Vertex,Edge>> solve() throws IOException, LpSolveException{
		Parameters.report.writeln("Creating LP Model\n. . .");
		
		String prefix = "lpmodel";
	    String suffix = ".tmp";
	    
	    File tempFile = File.createTempFile(prefix, suffix);
	    tempFile.deleteOnExit();
	    
	    FileWriter writer = new FileWriter(tempFile);
	    String stringLpModel = generateLPFormatString();
	    writer.append(stringLpModel);
	    
		Parameters.report.writeln("The LP model has been created: ");
		Parameters.report.writeln(stringLpModel);
	    
	    writer.flush();
	    writer.close();

		Parameters.report.writeln("Solving Lp Model\n. . .");

	    
	    //Solve lp model	
    	LpSolve solver = LpSolve.readLp(tempFile.getCanonicalFile().getAbsolutePath(),1,"");
    	int solverResult = solver.solve();

	      
	      ListenableDirectedWeightedGraph<Vertex,Edge> resultGraph = generateGraphFromSolution(solver.getPtrVariables());
	      

	      
	      Parameters.report.writeln("The Lp model was solved: ");
	      // print solution
	      Parameters.report.writeln("Value of objective function: " + solver.getObjective());
	      double[] var = solver.getPtrVariables();		     
	      for (int i = 0; i < var.length; i++) {
	    	  Parameters.report.writeln("Value of var[" + solver.getColName(i+1) + "] = " + var[i]);
	      }

	      // delete the problem and free memory
	      solver.deleteLp();
	      
	      return new Pair<Integer,ListenableDirectedWeightedGraph<Vertex,Edge>>(solverResult,resultGraph);
	}
	 
	public String generateLPFormatString(){
		//look for all simple path between node 0 and node 3
		List<GraphPath<Vertex,Edge>> paths = getAllSinglePath(graph, vertexs[0],vertexs[vertexs.length - 1 ]);

		StringBuilder st = new StringBuilder();
		
		st.append("min:");
		//plus between all enables edges and its cost
		for(int i=0; i<edges.length;i++)
			st.append(" +" + graph.getEdgeWeight(edges[i]) +" E"+i );
		st.append(";\n");
		
		//amount of path constraint
		st.append("r_1:");
		for(int i=0; i<paths.size();i++)
			st.append(" +P"+i);
		st.append(" >= " + AMOUNT_OF_PATH + ";\n");
		
		//constraint about each path
		for(int i=0;i<paths.size();i++){
			st.append("r_"+ (i+2) +":");
			st.append("+" + paths.get(i).getEdgeList().size() + " P"+ i + " <=");
			for(int j=0; j<paths.get(i).getEdgeList().size(); j++)
				st.append(" +" + paths.get(i).getEdgeList().get(j).name);
			st.append(";\n");
		}
		st.append("\n");
		
		//constraint that all variables are boolean.				
		st.append("bin");
		for(int i=0; i<paths.size();i++)
			st.append(" P" + i + ",");

		for(int i=0; i<graph.edgeSet().size();i++)
			st.append(" E" + i + ",");
		st.deleteCharAt(st.length()-1);
		st.append(";");
		
		System.out.println(st.toString());
		return st.toString();

	}
	
	public static List<GraphPath<Vertex, Edge>> getAllSinglePath(ListenableDirectedWeightedGraph<Vertex,Edge> graph, Vertex source, Vertex target){
		AllDirectedPaths<Vertex,Edge> pathFinder = new AllDirectedPaths<Vertex,Edge>(graph);
		List<GraphPath<Vertex, Edge>> paths = pathFinder.getAllPaths(source,target,true,null);
		
		Edge sourceToTarget = graph.getEdge(source,target);
		if (sourceToTarget != null ){ 			
			LinkedList<Edge> edgeList = new LinkedList<Edge>(); edgeList.add(sourceToTarget);
			paths.add(new GraphPathImpl<Vertex,Edge>(graph, source, target, edgeList, sourceToTarget.weight));
		
		}
		return paths;
	}
	
	public static LpModel testLpModel(ListenableDirectedWeightedGraph<Vertex,Edge> graph, int amount_of_path,Vertex[] vertexs, Edge[] edges) throws IOException, LpSolveException{
		return new LpModel(graph.vertexSet().size(), amount_of_path, Integer.MAX_VALUE, 100, graph, vertexs,edges);
	}
	
//	public static void main(String[] args) throws IOException{
//		LpModel model = new LpModel();
//				
//	}
//	
//	  public static void main(String[] args) throws IOException
//	  {
//	    String prefix = "lpmodel";
//	    String suffix = ".tmp";
//	     
//	    exampleGraph();
//	    
//	    File tempFile = File.createTempFile(prefix, suffix);
//	    tempFile.deleteOnExit();
//	    System.out.format("Canonical filename: %s\n", tempFile.getCanonicalFile());
//	    
//	    FileWriter writer = new FileWriter(tempFile);
//	    //Model for graph first example.
//	    
////	    writer.append(exampleGraph());
//	    writer.append(
//	    				"min: +1 E0 +1 E1 +2 E2 +1 E3 +1 E4;\n"+
//						"r_1: +P0 +P1 +P2 >= 2;\n"+
//						"r_2: +2 P0 <= +E0 +E4;\n"+
//						"r_3: +2 P1 <= +E1 +E3;\n"+
//						"r_4: +3 P2 <= +E1 +E2 +E4;\n"+
//						"\n"+	
//						"bin E0, E1, E2, E3, E4, P0, P1, P2;"
//					);
////	    writer.append(
////	    				"max: 143 x + 60 y;\n"+
////	    				"120 x + 210 y <= 15000;\n"+
////	    				"110 x + 30 y <= 4000;\n"+
////	    				"x + y <= 75;\n"
////	    				);
//
////	    writer.append(
////	    				"min: -x1 -2 x2 +0.1 x3 +3 x4; \n"+
////	    				"r_1: +x1 +x2 <= 5; \n"+
////						"r_2: +2 x1 -x2 >= 0; \n"+
////						"r_3: -x1 +3 x2 >= 0; \n"+
////						"r_4: +x3 +x4 >= 0.5; \n"+
////						"\n"+	
////						"bin x3, x4;\n"
////					);
//	    writer.flush();
//	    writer.close();
//	    try{
//	    	LpSolve solver = LpSolve.readLp(tempFile.getCanonicalFile().getAbsolutePath(),1,"");
//	    	solver.solve();
//		      // print solution
//		      System.out.println("Value of objective function: " + solver.getObjective());
//		      double[] var = solver.getPtrVariables();		     
//		      for (int i = 0; i < var.length; i++) {
//		        System.out.println("Value of var[" + solver.getColName(i+1) + "] = " + var[i]);
//		      }
//		      
//		      
//		      // delete the problem and free memory
//		      solver.deleteLp();
//	    }catch(LpSolveException e){
//	    	System.out.println(e.toString());
//	    }
//	}
	
/*
	  public static void main(String[] args) {
	    try {
	      // Create a problem with 4 variables and 0 constraints
	      LpSolve solver = LpSolve.makeLp(0, 4);

	      // add constraints
	      solver.strAddConstraint("3 2 2 1", LpSolve.LE, 4);
	      solver.strAddConstraint("0 4 3 1", LpSolve.GE, 3);

	      // set objective function
	      solver.strSetObjFn("2 3 -2 3");
	      
	      solver.setMaxim();
	      // solve the problem
	      solver.solve();

	      // print solution
	      System.out.println("Value of objective function: " + solver.getObjective());
	      double[] var = solver.getPtrVariables();
	      for (int i = 0; i < var.length; i++) {
	        System.out.println("Value of var[" + i + "] = " + var[i]);
	      }

	      // delete the problem and free memory
	      solver.deleteLp();
	    }
	    catch (LpSolveException e) {
	       e.printStackTrace();
	    }
	  }
*/

}
