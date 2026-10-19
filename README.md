# Ascendisk — binary-options signal indicator

Ascendisk is a Spring Boot + React application that turns live 1-minute (M1) forex data into
decision-support signals for binary-options traders. It computes a convergence precision score and
only lights a directional signal once the score reaches the 75% threshold. Orders are always placed
manually on the broker; Ascendisk only helps decide and then tracks them.
