package ca.carleton.michelleburrows.groupbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.impl.CrossoverOperator;

public class PermutationCrossover extends CrossoverOperator {
	private static final long serialVersionUID = 5641120745922656585L;
	private final double crossoverRate;
	
	public PermutationCrossover(final Configuration config, double crossoverRate) throws InvalidConfigurationException {
		super(config);
		this.crossoverRate = crossoverRate;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void operate(Population pop, List candidates) {
		int numCrossovers = (int)(pop.size()*crossoverRate);
		Random rand = new Random();
		for (int i = 0; i < numCrossovers; ++i) {
			int index1, index2;
			IChromosome chrom1, chrom2;
			/*Keep picking random chromosomes until we get two different ones
			 * that aren't brand new */
			do {
				index1 = rand.nextInt(pop.size());
				index2 = rand.nextInt(pop.size());
				chrom1 = pop.getChromosome(index1);
				chrom2 = pop.getChromosome(index2);
			} while (chrom1 == chrom2/* || chrom1.getAge() < 1 || 
					chrom2.getAge() < 1*/);
			
			IChromosome parent1 = (IChromosome) chrom1.clone();
			IChromosome parent2 = (IChromosome) chrom2.clone();
			try {
				doCrossover(parent1, parent2, candidates, rand);
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
				System.out.println("\nError configuring genetic algorithm");
				System.exit(1);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doCrossover(IChromosome chrom1, IChromosome chrom2, List candidates, Random rand)
			throws InvalidConfigurationException {
		//for each composite gene, crossover the sub-genes
			
			List<Gene> par1 = Arrays.asList(chrom1.getGenes());
			List<Gene> par2 = Arrays.asList(chrom2.getGenes());
			List<Gene> child1 = new ArrayList<Gene>(par1.size());
			List<Gene> child2 = new ArrayList<Gene>(par2.size());
			for (int j = 0; j < par1.size(); ++j) {
				child1.add(null);
				child2.add(null);
			}
			doPartiallyMappedCrossover(par1, par2, child1, child2, rand);

		chrom1.setGenes(child1.toArray(new Gene[child1.size()]));
		chrom2.setGenes(child2.toArray(new Gene[child2.size()]));
		candidates.add(chrom1);
		candidates.add(chrom2);
	}
	
	/**
	 * Perform a Partially Mapped Crossover between two chromosomes
	 * @param par1 the genes of the first parent
	 * @param par2 the genes of the second parent
	 * @param child1 the genes for the first child
	 * @param child2 the genes for the second child
	 * @param rand a random number generator
	 */
	private void doPartiallyMappedCrossover(List<Gene> par1, List<Gene> par2, List<Gene> child1, List<Gene> child2, Random rand) {
		//TODO update for CompositeGene
		int numGenes = par1.size();
		
		List<Gene> used1 = new ArrayList<Gene>();
		List<Gene> used2 = new ArrayList<Gene>();
		List<Gene> need1 = new ArrayList<Gene>();
		List<Gene> need2 = new ArrayList<Gene>();
		
		int start = rand.nextInt(numGenes-1);
		int end = start + rand.nextInt(numGenes - start) + 1;

		for (int i = start; i < end; ++i) {
			child1.set(i, par1.get(i));
			child2.set(i, par2.get(i));
			used1.add(par1.get(i));
			used2.add(par2.get(i));
		}
		for (int i = start; i < end; ++i) {
			if (!used1.contains(par2.get(i))) {
				need1.add(par2.get(i));
			}
			if (!used2.contains(par1.get(i))) {
				need2.add(par1.get(i));
			}
		}
		for (int j = 0; j < numGenes - (end - start); ++j) {
			int i = (j+end)%numGenes;
			if (used1.contains(par2.get(i))) {
				child1.set(i, need1.get(0));
				need1.remove(0);
			} else {
				child1.set(i, par2.get(i));
			}

			if (used2.contains(par1.get(i))) {
				child2.set(i, need2.get(0));
				need2.remove(0);
			} else {
				child2.set(i, par1.get(i));
			}
			
		}
	}

}
