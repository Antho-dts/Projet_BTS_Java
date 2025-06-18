//--------------------- Package and Imports -----------------------//
package Fenetre_Graphique_Monitoring;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import Utils.Tempo;
import java.awt.Color;

//--------------------- Main Class -----------------------//
public class ThreadJFreeChart extends Thread {

    //--------------------- Attributes -----------------------//
    private boolean IsPulse;
    private double value = 0;
    private long m_starttime = System.currentTimeMillis();

    private XYSeries serie = new XYSeries("Value", true, false);

    private PatientMonitorThread monThread_Patient;
    private ChartPanel monChart = null;

    private int Cpt = 0;
    private static int NbThread = 0;
    private int NumThread = 0;

    //--------------------- Constructor -----------------------//
    public ThreadJFreeChart(PatientMonitorThread _Thread_Patient, boolean _IsPulse) {
        this.monThread_Patient = _Thread_Patient;
        this.IsPulse = _IsPulse;

        NbThread++;
        this.NumThread = NbThread;

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(this.serie);

        JFreeChart chart = ChartFactory.createXYLineChart(
            IsPulse ? "Graph Heart Rate" : "Graph Blood Oxygen",
            "Time (ms)",
            IsPulse ? "Beats per Minute (bpm)" : "Rate (%)",
            dataset
        );

        //--------------------- Chart Customization -----------------------//
        XYPlot plot = chart.getXYPlot();
        if (!this.IsPulse) {
            plot.getRangeAxis().setRange(0.0, 110.0);
        } else {
        	plot.getRangeAxis().setRange(0.0, 250.0);
        }

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        renderer.setSeriesPaint(0, this.IsPulse ? Color.RED : Color.BLUE);
        renderer.setSeriesShapesVisible(0, false);

        plot.setRenderer(renderer);

        this.monChart = new ChartPanel(chart);
        this.serie.setMaximumItemCount(100); // Limit visible points
    }

    //--------------------- Main Thread Loop -----------------------//
    @Override
    public void run() {
        System.out.println("NbThread " + NbThread + " NumThread: " + this.NumThread);
        try {
            while (!interrupted()) {
                double time = (double) System.currentTimeMillis() - this.m_starttime;

                if (!this.IsPulse) {
                    this.value = monThread_Patient.getPatientData().getHeartRate();
                } else {
                    this.value = monThread_Patient.getPatientData().getBloodOxygen();
                }

                final double t = time;
                final double red = value;

                javax.swing.SwingUtilities.invokeLater(() -> {
                    this.serie.add(t, red);
                    this.Cpt++;
                });

                new Tempo(100); // Refresh rate
            }
        } catch (Exception e) {
            System.err.println("Erreur dans ThreadJFreeChart : " + e.getMessage());
        }
    }

    //--------------------- Utility Methods -----------------------//
    public void close() {
        new Tempo(500);
        this.interrupt();
    }

    public void clearChart() {
        synchronized (serie) {
            this.serie.clear();
        }
    }

    //--------------------- Getter -----------------------//
    public ChartPanel getChart() {
        return this.monChart;
    }
}
