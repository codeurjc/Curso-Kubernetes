# Pasos previos a la sesión

Por favor, sigue estos pasos antes de la sesión para que sea más ágil comenzar. En resumen, de lo que se trata es de tener la instancia de Minikube con las imágenes Docker descargadas para que sea más rápido empezar.

## Arranca Minikube

Sigue las instrucciones de instalación según tu Sistema operativo:

* Linux

https://kubernetes.io/docs/tasks/tools/install-minikube/#linux

* Windows

https://kubernetes.io/docs/tasks/tools/install-minikube/#windows

* Mac

https://kubernetes.io/docs/tasks/tools/install-minikube/#macos

Al finalizar arranca Minikube con al menos 4GB de RAM y 4CPUs

`$ minikube start --memory=4098 --cpus=4`

## Conenctate a Minikube

Para conectarnos a la máquina virtual que corre Minikube hacemos:

`$ minikube ssh` 

## Ejecuta esta línea

`curl -s https://raw.githubusercontent.com/codeurjc/Curso-Kubernetes/master/docker-pull.sh | bash`

