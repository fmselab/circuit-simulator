package components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.StringTokenizer;

import simulator.CirSim;
import utils.EditInfo;

public class LampElm extends CircuitElm {
	double resistance;
	final double roomTemp = 300;
	double temp, nom_pow, nom_v, warmTime, coolTime;

	public LampElm(int xx, int yy) {
		super(xx, yy);
		temp = roomTemp;
		nom_pow = 100;
		nom_v = 120;
		warmTime = .4;
		coolTime = .4;
	}

	public LampElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		temp = new Double(st.nextToken()).doubleValue();
		nom_pow = new Double(st.nextToken()).doubleValue();
		nom_v = new Double(st.nextToken()).doubleValue();
		warmTime = new Double(st.nextToken()).doubleValue();
		coolTime = new Double(st.nextToken()).doubleValue();
	}

	@Override
	public String dump() {
		return super.dump() + " " + temp + " " + nom_pow + " " + nom_v + " " + warmTime + " " + coolTime;
	}

	@Override
	public int getDumpType() {
		return 181;
	}

	Point bulbLead[], filament[], bulb;
	int bulbR;

	@Override
	public void reset() {
		super.reset();
		temp = roomTemp;
	}

	final int filament_len = 24;

	@Override
	public void setPoints() {
		super.setPoints();
		int llen = 16;
		calcLeads(llen);
		bulbLead = newPointArray(2);
		filament = newPointArray(2);
		bulbR = 20;
		filament[0] = interpPoint(lead1, lead2, 0, filament_len);
		filament[1] = interpPoint(lead1, lead2, 1, filament_len);
		double br = filament_len - Math.sqrt(bulbR * bulbR - llen * llen);
		bulbLead[0] = interpPoint(lead1, lead2, 0, br);
		bulbLead[1] = interpPoint(lead1, lead2, 1, br);
		bulb = interpPoint(filament[0], filament[1], .5);
	}

	Color getTempColor() {
		if (temp < 1200) {
			int x = (int) (255 * (temp - 800) / 400);
			if (x < 0)
				x = 0;
			return new Color(x, 0, 0);
		}
		if (temp < 1700) {
			int x = (int) (255 * (temp - 1200) / 500);
			if (x < 0)
				x = 0;
			return new Color(255, x, 0);
		}
		if (temp < 2400) {
			int x = (int) (255 * (temp - 1700) / 700);
			if (x < 0)
				x = 0;
			return new Color(255, 255, x);
		}
		return Color.white;
	}

	@Override
	public void draw(Graphics g) {
		double v1 = volts[0];
		double v2 = volts[1];
		setBbox(point1, point2, 4);
		adjustBbox(bulb.x - bulbR, bulb.y - bulbR, bulb.x + bulbR, bulb.y + bulbR);
		// adjustbbox
		draw2Leads(g);
		setPowerColor(g, true);
		g.setColor(getTempColor());
		g.fillOval(bulb.x - bulbR, bulb.y - bulbR, bulbR * 2, bulbR * 2);
		g.setColor(Color.white);
		drawThickCircle(g, bulb.x, bulb.y, bulbR);
		setVoltageColor(g, v1);
		drawThickLine(g, lead1, filament[0]);
		setVoltageColor(g, v2);
		drawThickLine(g, lead2, filament[1]);
		setVoltageColor(g, (v1 + v2) * .5);
		drawThickLine(g, filament[0], filament[1]);
		updateDotCount();
		if (sim.getDragElm() != this) {
			drawDots(g, point1, lead1, curcount);
			double cc = curcount + (dn - 16) / 2;
			drawDots(g, lead1, filament[0], cc);
			cc += filament_len;
			drawDots(g, filament[0], filament[1], cc);
			cc += 16;
			drawDots(g, filament[1], lead2, cc);
			cc += filament_len;
			drawDots(g, lead2, point2, curcount);
		}
		drawPosts(g);
	}

	@Override
	void calculateCurrent() {
		current = (volts[0] - volts[1]) / resistance;
		// System.out.print(this + " res current set to " + current + "\n");
	}

	@Override
	public void stamp() {
		sim.stampNonLinear(nodes[0]);
		sim.stampNonLinear(nodes[1]);
	}

	@Override
	public boolean nonLinear() {
		return true;
	}

	@Override
	public void startIteration() {
		// based on http://www.intusoft.com/nlpdf/nl11.pdf
		double nom_r = nom_v * nom_v / nom_pow;
		// this formula doesn't work for values over 5390
		double tp = (temp > 5390) ? 5390 : temp;
		resistance = nom_r * (1.26104 - 4.90662 * Math.sqrt(17.1839 / tp - 0.00318794) - 7.8569 / (tp - 187.56));
		double cap = 1.57e-4 * nom_pow;
		double capw = cap * warmTime / .4;
		double capc = cap * coolTime / .4;
		// System.out.println(nom_r + " " + (resistance/nom_r));
		temp += getPower() * sim.getTimeStep() / capw;
		double cr = 2600 / nom_pow;
		temp -= sim.getTimeStep() * (temp - roomTemp) / (capc * cr);
		// System.out.println(capw + " " + capc + " " + temp + " " +resistance);
	}

	@Override
	public void doStep() {
		sim.stampResistor(nodes[0], nodes[1], resistance);
	}

	@Override
	public void getInfo(String arr[]) {
		arr[0] = "lamp";
		getBasicInfo(arr);
		arr[3] = "R = " + getUnitText(resistance, CirSim.getOhmString());
		arr[4] = "P = " + getUnitText(getPower(), "W");
		arr[5] = "T = " + ((int) temp) + " K";
	}

	@Override
	public EditInfo getEditInfo(int n) {
		// ohmString doesn't work here on linux
		if (n == 0)
			return new EditInfo("Nominal Power", nom_pow, 0, 0);
		if (n == 1)
			return new EditInfo("Nominal Voltage", nom_v, 0, 0);
		if (n == 2)
			return new EditInfo("Warmup Time (s)", warmTime, 0, 0);
		if (n == 3)
			return new EditInfo("Cooldown Time (s)", coolTime, 0, 0);
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		if (n == 0 && ei.getValue() > 0)
			nom_pow = ei.getValue();
		if (n == 1 && ei.getValue() > 0)
			nom_v = ei.getValue();
		if (n == 2 && ei.getValue() > 0)
			warmTime = ei.getValue();
		if (n == 3 && ei.getValue() > 0)
			coolTime = ei.getValue();
	}
}
