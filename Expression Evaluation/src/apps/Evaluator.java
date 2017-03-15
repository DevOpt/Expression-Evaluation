package apps;

import java.io.IOException;
import java.util.Scanner;

public class Evaluator {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		while (true) {
			System.out.print("\nEnter the expression, or hit return to quit => ");
			String line = sc.nextLine();
			if (line.length() == 0) {
				break;
			}
			Expression expr = new Expression(line);
			expr.buildSymbols();

			expr.printScalars();
			expr.printArrays();
		}
		sc.close();
	}
}
