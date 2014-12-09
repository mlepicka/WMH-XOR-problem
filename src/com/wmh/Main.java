package com.wmh;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.competitive.CompetitiveTraining;
import org.encog.neural.networks.training.competitive.neighborhood.NeighborhoodFunction;
import org.encog.neural.networks.training.competitive.neighborhood.NeighborhoodSingle;
import org.encog.neural.networks.training.hebbian.HebbianTraining;
import org.encog.neural.networks.training.propagation.back.Backpropagation;

public class Main {

/**
 * The input necessary for XOR.
 */
        public static double XOR_INPUT[][] = { { 0.0, 0.0 }, { 1.0, 0.0 },
                { 0.0, 1.0 }, { 1.0, 1.0 } };

        /**
         * The ideal data necessary for XOR.
         */
        public static double XOR_IDEAL[][] = { { 0.0 }, { 1.0 }, { 1.0 }, { 0.0 } };

        /**
         * The main method.
         * @param args No arguments are used.
         */
        public static void main(final String args[]) {

            // create a neural network, without using a factory
            BasicNetwork network = new BasicNetwork();
            network.addLayer(new BasicLayer(null, true, 2));
            network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 3));
            network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
            network.getStructure().finalizeStructure();
            network.reset();

            // create training data
           // MLDataSet trainingSet = new BasicMLDataSet(XOR_INPUT, XOR_IDEAL);
            NeuralDataSet dataSet = new BasicNeuralDataSet(XOR_INPUT, XOR_IDEAL);

            // backpropagation the neural network
            //final ResilientPropagation backpropagation = new ResilientPropagation(network, trainingSet);

            //back propagation
            final Backpropagation backpropagation = new Backpropagation(network, dataSet);
            int epoch = 1;
            //ONE
            do {
                backpropagation.iteration();
                System.out.println("Epoch #" + epoch + " Error:" + backpropagation.getError());
                epoch++;
            } while (backpropagation.getError() > 0.01);
            backpropagation.finishTraining();

            // test the neural network
            System.out.println("Neural Network Results:");
            for (NeuralDataPair pair : dataSet) {
                final NeuralData output = network.compute(pair.getInput());
                System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                        + ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
            }
            network.reset();
            //TWO
            //hebbian training
            final HebbianTraining hebbianTraining = new HebbianTraining(network, dataSet, false, 0.01);
            do {
                hebbianTraining.iteration();
                System.out.println("Epoch #" + epoch + " Error:" + hebbianTraining.getError());
                epoch++;
            } while (hebbianTraining.getError() > 0.01);
            hebbianTraining.finishTraining();

            // test the neural network
            System.out.println("Neural Network Results:");
            for (NeuralDataPair pair : dataSet) {
                final NeuralData output = network.compute(pair.getInput());
                System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                        + ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
            }
            network.reset();
            //THREE
            //WTA
            NeighborhoodFunction neighborhoodFunction = new NeighborhoodSingle();
            final CompetitiveTraining competitiveTraining = new CompetitiveTraining(network,0.01,  dataSet, neighborhoodFunction);
            do {
                backpropagation.iteration();
                System.out.println("Epoch #" + epoch + " Error:" + backpropagation.getError());
                epoch++;
            } while (backpropagation.getError() > 0.01);
            backpropagation.finishTraining();

            // test the neural network
            System.out.println("Neural Network Results:");
            for (NeuralDataPair pair : dataSet) {
                final NeuralData output = network.compute(pair.getInput());
                System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
                        + ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
            }


            Encog.getInstance().shutdown();
        }
}
