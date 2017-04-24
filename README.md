# Graph-based-Skill-Acquisition
I have implemented some of famous algorithms for making skills using the Option framework in Reinforcement Learning

for running the code from the command line, you should select the environment and skill acquisition algorithm. For grid world domain the default world is 6-room, but you can change it according the domains defined in calss sharif.ce.isl.rl.graph.environment.Grids. The parameters for SCC algorithm is set in the constructor of class sharif.ce.isl.rl.graph.algorithm.SCC, but you can change it.

1) running SCC algorithm for making skills:
-alg sharif.ce.isl.rl.graph.algorithm.SCC -env sharif.ce.isl.rl.graph.environment.GridWorldEnv -iniStat 1,1 -finStat 30,23 -numEpisode 100

2) running Q-learning algorithm:
-alg sharif.ce.isl.rl.graph.algorithm.core.QLearningAgent -env sharif.ce.isl.rl.graph.environment.TaxiDriverEnv -numEpisode 100

%For using this code please cite to the following paper:
%

for any question, please contact with nasrin.taghizadeh@gmail.com.
