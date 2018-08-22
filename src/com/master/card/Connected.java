package com.master.card;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Connected
 */
@WebServlet(description = "Retrieves if a road exists between two given cities", urlPatterns = {
		"/connected" }, initParams = {
				@WebInitParam(name = "CitiesList", value = "city.txt", description = "List of connected cities") })
public class Connected extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String[] citiesList;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Connected() {
		super();
		citiesList = null;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		String citiesFile = getServletConfig().getInitParameter("CitiesList");

		try {

			ServletContext context = getServletContext();
			String systemPath = context.getRealPath(citiesFile);

			BufferedReader br = new BufferedReader(new FileReader(new File(systemPath)));

			String connectedPair;

			ArrayList<String> citiesAL = new ArrayList<>();

			while ((connectedPair = br.readLine()) != null) {
				citiesAL.add(connectedPair);
			}

			br.close();

			citiesList = new String[citiesAL.size()];
			citiesList = citiesAL.toArray(citiesList);

		} catch (FileNotFoundException e) {
			System.err.println("Problem finding file: " + citiesFile + " Details: " + e);
		} catch (IOException e) {
			System.err.println("Problem reading file: " + citiesFile + " Details: " + e);
		}

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		boolean debugOn = false;

		PrintWriter out = response.getWriter();

		String origin = request.getParameter("origin").trim();
		String destination = request.getParameter("destination").trim();

		if (debugOn) {
			out.println(citiesList[0]);
			out.println(citiesList[1]);
			out.println(citiesList[2]);
			out.println(citiesList[3]);
			out.println("Origin is: " + origin);
			out.println("Destination is: " + destination);
		}

		boolean cityPairFound = false; // assume until known otherwise

		if ((origin.length() != 0) && (destination.length() != 0)) // basic city to city direct test
		{
			for (String cityPair : citiesList) {
				// Assumed several conditions:
				// city pairs are different i.e. we never have NewYork,NewYork as a pairing
				// we assume no one way roads, hence: NewYork,Boston is valid in either direction
				// Although not clarified, I will assume that origin: Boston and Destination: Boston is valid.

				String[] pairing = cityPair.split(",");
				pairing[0] = pairing[0].trim();
				pairing[1] = pairing[1].trim();

				if (((pairing[0].equals(origin)) || (pairing[1].equals(origin)))
						&& ((pairing[0].equals(destination)) || (pairing[1].equals(destination)))) {
					cityPairFound = true; // direct city pair found
					break;
				}

			}
		}

		if (!cityPairFound) // lets try multi city routing....
		{
			cityPairFound = multiCityCheck(origin, destination);
		}

		if (cityPairFound) {
			response.getWriter().append("yes");
		} else {
			response.getWriter().append("no");
		}

	}

	private boolean multiCityCheck(String origin, String destination) {

		boolean debugOn = false;

		boolean sisterCityFound = false; // assume until found
		String[] sisterCities = new String[citiesList.length];
		int currentSisterCount = 0;

		for (String cityPair : citiesList) {

			String[] pairing = cityPair.split(",");
			pairing[0] = pairing[0].trim();
			pairing[1] = pairing[1].trim();

			if (debugOn) {
				System.out.println("Checking pairs org/dest [0][1]" + origin + "/" + destination + "-" + pairing[0]
						+ "- " + pairing[1]);
			}

			if (pairing[0].equals(origin)) {
				sisterCities[currentSisterCount++] = pairing[1];
				if (debugOn) {
					System.out.println("PARING[0] FOUND: " + sisterCities[currentSisterCount - 1]);
				}
			}
			if (pairing[1].equals(origin)) {
				sisterCities[currentSisterCount++] = pairing[0];
				if (debugOn) {
					System.out.println("PARING[1] FOUND: " + sisterCities[currentSisterCount - 1]);
				}
			}

			for (String sisters : sisterCities) {

				if ((null != sisters) && (debugOn)) {
					System.out.println("a sister is: " + sisters);
				}
				if ((null != sisters) && (sisters.trim().equals(destination))) {
					sisterCityFound = true;
					break;
				}
			}
		}

		if (!(sisterCityFound)) {
			sisterCityFound = findMatchingSister(sisterCities, destination);
		}
		return sisterCityFound;
	}

	public boolean findMatchingSister(String[] sisterCities, String destination) {
		boolean cityPairFound = false;
		for (String origin : sisterCities) {

			for (String cityPair : citiesList) {

				String[] pairing = cityPair.split(",");
				pairing[0] = pairing[0].trim();
				pairing[1] = pairing[1].trim();

				if (((pairing[0].equals(origin)) || (pairing[1].equals(origin)))
						&& ((pairing[0].equals(destination)) || (pairing[1].equals(destination)))) {
					cityPairFound = true; // sister city pair routing found
					break;
				}
			}
		}
		return cityPairFound;
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	// protected void doPost(HttpServletRequest request, HttpServletResponse
	// response) throws ServletException, IOException {
	// // TODO Auto-generated method stub
	// doGet(request, response); // do not do as we only want one endpoint -
	// hence we cannot be called by doPost
	// }

}
