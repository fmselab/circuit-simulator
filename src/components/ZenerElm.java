package components;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

import utils.EditInfo;

// Zener code contributed by J. Mike Rollins
// http://www.camotruck.net/rollins/simulator.html
public class ZenerElm extends DiodeElm {
	public ZenerElm(int xx, int yy) {
		super(xx, yy);
		zvoltage = default_zvoltage;
		setup();
	}

	public ZenerElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
		zvoltage = new Double(st.nextToken()).doubleValue();
		setup();
	}

	@Override
	void setup() {
		diode.leakage = 5e-6; // 1N4004 is 5.0 uAmp
		super.setup();
	}

	@Override
	public int getDumpType() {
		return 'z';
	}

	@Override
	public String dump() {
		return super.dump() + " " + zvoltage;
	}

	final int hs = 8;
	Polygon poly;
	Point cathode[];
	Point wing[];

	@Override
	public void setPoints() {
		super.setPoints();
		calcLeads(16);
		cathode = newPointArray(2);
		wing = newPointArray(2);
		Point pa[] = newPointArray(2);
		interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
		interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
		interpPoint(cathode[0], cathode[1], wing[0], -0.2, -hs);
		interpPoint(cathode[1], cathode[0], wing[1], -0.2, -hs);
		poly = createPolygon(pa[0], pa[1], lead2);
	}

	@Override
	public void draw(Graphics g) {
		setBbox(point1, point2, hs);

		double v1 = volts[0];
		double v2 = volts[1];

		draw2Leads(g);

		// draw arrow thingy
		setPowerColor(g, true);
		setVoltageColor(g, v1);
		g.fillPolygon(poly);

		// draw thing arrow is pointing to
		setVoltageColor(g, v2);
		drawThickLine(g, cathode[0], cathode[1]);

		// draw wings on cathode
		drawThickLine(g, wing[0], cathode[0]);
		drawThickLine(g, wing[1], cathode[1]);

		doDots(g);
		drawPosts(g);
	}

	final double default_zvoltage = 5.6;

	@Override
	public void getInfo(String arr[]) {
		super.getInfo(arr);
		arr[0] = "Zener diode";
		arr[5] = "Vz = " + getVoltageText(zvoltage);
	}

	@Override
	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Fwd Voltage @ 1A", fwdrop, 10, 1000);
		if (n == 1)
			return new EditInfo("Zener Voltage @ 5mA", zvoltage, 1, 25);
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			fwdrop = ei.getValue();
		if (n == 1)
			zvoltage = ei.getValue();
		setup();
	}

	@Override
	public int getShortcut() {
		return 0;
	}
}
