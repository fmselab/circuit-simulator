package components;

import java.awt.Graphics;
import java.awt.Label;
import java.awt.Point;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.StringTokenizer;

import simulator.CirSim;
import utils.EditInfo;

public class PotElm extends CircuitElm implements AdjustmentListener {
	double position, maxResistance, resistance1, resistance2;
	double current1, current2, current3;
	double curcount1, curcount2, curcount3;
	Scrollbar slider;
	Label label;
	String sliderText;

	public PotElm(int xx, int yy) {
		super(xx, yy);
		setup();
		maxResistance = 1000;
		position = .5;
		sliderText = "Resistance";
		createSlider();
	}

	public PotElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		maxResistance = new Double(st.nextToken()).doubleValue();
		position = new Double(st.nextToken()).doubleValue();
		sliderText = st.nextToken();
		while (st.hasMoreTokens())
			sliderText += ' ' + st.nextToken();
		createSlider();
	}

	void setup() {
	}

	@Override
	public int getPostCount() {
		return 3;
	}

	@Override
	public int getDumpType() {
		return 174;
	}

	@Override
	public Point getPost(int n) {
		return (n == 0) ? point1 : (n == 1) ? point2 : post3;
	}

	@Override
	public String dump() {
		return super.dump() + " " + maxResistance + " " + position + " " + sliderText;
	}

	void createSlider() {
		CirSim.getMain().add(label = new Label(sliderText, Label.CENTER));
		int value = (int) (position * 100);
		CirSim.getMain().add(slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101));
		CirSim.getMain().validate();
		slider.addAdjustmentListener(this);
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		sim.setAnalyzeFlag(true);
		setPoints();
	}

	@Override
	public void delete() {
		CirSim.getMain().remove(label);
		CirSim.getMain().remove(slider);
	}

	Point post3, corner2, arrowPoint, midpoint, arrow1, arrow2;
	Point ps3, ps4;
	int bodyLen;

	@Override
	public void setPoints() {
		super.setPoints();
		int offset = 0;
		if (abs(dx) > abs(dy)) {
			dx = sim.snapGrid(dx / 2) * 2;
			point2.x = setX2(point1.x + dx);
			offset = (dx < 0) ? dy : -dy;
			point2.y = point1.y;
		} else {
			dy = sim.snapGrid(dy / 2) * 2;
			point2.y = setY2(point1.y + dy);
			offset = (dy > 0) ? dx : -dx;
			point2.x = point1.x;
		}
		if (offset == 0)
			offset = sim.getGridSize();
		dn = distance(point1, point2);
		int bodyLen = 32;
		calcLeads(bodyLen);
		position = slider.getValue() * .0099 + .005;
		int soff = (int) ((position - .5) * bodyLen);
		// int offset2 = offset - sign(offset)*4;
		post3 = interpPoint(point1, point2, .5, offset);
		corner2 = interpPoint(point1, point2, soff / dn + .5, offset);
		arrowPoint = interpPoint(point1, point2, soff / dn + .5, 8 * sign(offset));
		midpoint = interpPoint(point1, point2, soff / dn + .5);
		arrow1 = new Point();
		arrow2 = new Point();
		double clen = abs(offset) - 8;
		interpPoint2(corner2, arrowPoint, arrow1, arrow2, (clen - 8) / clen, 8);
		ps3 = new Point();
		ps4 = new Point();
	}

	@Override
	public void draw(Graphics g) {
		int segments = 16;
		int i;
		int ox = 0;
		int hs = sim.getEuroResistorCheckItem().getState() ? 6 : 8;
		double v1 = volts[0];
		double v2 = volts[1];
		double v3 = volts[2];
		setBbox(point1, point2, hs);
		draw2Leads(g);
		setPowerColor(g, true);
		double segf = 1. / segments;
		int divide = (int) (segments * position);
		if (!sim.getEuroResistorCheckItem().getState()) {
			// draw zigzag
			for (i = 0; i != segments; i++) {
				int nx = 0;
				switch (i & 3) {
				case 0:
					nx = 1;
					break;
				case 2:
					nx = -1;
					break;
				default:
					nx = 0;
					break;
				}
				double v = v1 + (v3 - v1) * i / divide;
				if (i >= divide)
					v = v3 + (v2 - v3) * (i - divide) / (segments - divide);
				setVoltageColor(g, v);
				interpPoint(lead1, lead2, ps1, i * segf, hs * ox);
				interpPoint(lead1, lead2, ps2, (i + 1) * segf, hs * nx);
				drawThickLine(g, ps1, ps2);
				ox = nx;
			}
		} else {
			// draw rectangle
			setVoltageColor(g, v1);
			interpPoint2(lead1, lead2, ps1, ps2, 0, hs);
			drawThickLine(g, ps1, ps2);
			for (i = 0; i != segments; i++) {
				double v = v1 + (v3 - v1) * i / divide;
				if (i >= divide)
					v = v3 + (v2 - v3) * (i - divide) / (segments - divide);
				setVoltageColor(g, v);
				interpPoint2(lead1, lead2, ps1, ps2, i * segf, hs);
				interpPoint2(lead1, lead2, ps3, ps4, (i + 1) * segf, hs);
				drawThickLine(g, ps1, ps3);
				drawThickLine(g, ps2, ps4);
			}
			interpPoint2(lead1, lead2, ps1, ps2, 1, hs);
			drawThickLine(g, ps1, ps2);
		}
		setVoltageColor(g, v3);
		drawThickLine(g, post3, corner2);
		drawThickLine(g, corner2, arrowPoint);
		drawThickLine(g, arrow1, arrowPoint);
		drawThickLine(g, arrow2, arrowPoint);
		curcount1 = updateDotCount(current1, curcount1);
		curcount2 = updateDotCount(current2, curcount2);
		curcount3 = updateDotCount(current3, curcount3);
		if (sim.getDragElm() != this) {
			drawDots(g, point1, midpoint, curcount1);
			drawDots(g, point2, midpoint, curcount2);
			drawDots(g, post3, corner2, curcount3);
			drawDots(g, corner2, midpoint, curcount3 + distance(post3, corner2));
		}
		drawPosts(g);
	}

	@Override
	void calculateCurrent() {
		current1 = (volts[0] - volts[2]) / resistance1;
		current2 = (volts[1] - volts[2]) / resistance2;
		current3 = -current1 - current2;
	}

	@Override
	public void stamp() {
		resistance1 = maxResistance * position;
		resistance2 = maxResistance * (1 - position);
		sim.stampResistor(nodes[0], nodes[2], resistance1);
		sim.stampResistor(nodes[2], nodes[1], resistance2);
	}

	@Override
	public void getInfo(String arr[]) {
		arr[0] = "potentiometer";
		arr[1] = "Vd = " + getVoltageDText(getVoltageDiff());
		arr[2] = "R1 = " + getUnitText(resistance1, CirSim.getOhmString());
		arr[3] = "R2 = " + getUnitText(resistance2, CirSim.getOhmString());
		arr[4] = "I1 = " + getCurrentDText(current1);
		arr[5] = "I2 = " + getCurrentDText(current2);
	}

	@Override
	public EditInfo getEditInfo(int n) {
		// ohmString doesn't work here on linux
		if (n == 0)
			return new EditInfo("Resistance (ohms)", maxResistance, 0, 0);
		if (n == 1) {
			EditInfo ei = new EditInfo("Slider Text", 0, -1, -1);
			ei.setText(sliderText);
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			maxResistance = ei.getValue();
		if (n == 1) {
			sliderText = ei.getTextf().getText();
			label.setText(sliderText);
		}
	}

	@Override
	public void doStep() {
		// TODO Auto-generated method stub
		
	}
}
