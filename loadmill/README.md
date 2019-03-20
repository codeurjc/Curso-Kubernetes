# Loadmill Token Verify

**Nota** Esto funciona con istio porque el LoadBalancer es conocido a priori. Tenemos que publicar este deployment con istio.

Si queremos hacer uso del servicio de generación de tráfico [Loadmill](https://www.loadmill.com) tenemos que verificar que la URL está bajo nuestro control. Para ello, Loadmill proporciona un mecanismo de challenge.

En la plataforma de Loadmill accedemos a **Settings** -> **Domains** y veremos un formulario donde poder introducir la URL que queremos verificar (la que genera istio). Al hacer click en verficar nos aparece un cuadro con el token que debemos generar bajo nuestra URL. 

Por ejemplo, si nuestro token es el siguiente:

`9871ddb34786bbb8c2cb2d212811ce1a`

Tenemos que hacer lo siguiente:

1. Codificarlo a base 64

`echo -n 9871ddb34786bbb8c2cb2d212811ce1a | base64`

Que devuelve una cadena:

`OTg3MWRkYjM0Nzg2YmJiOGMyY2IyZDIxMjgxMWNlMWE=`

2. Copiar la cadena codificada en el fichero yaml.

En el fichero yaml, en la definición del secreto, sustituir esta cadena.

3. Crear el deployment

`$ kubectl create -f loadmill-token-verify.yaml`

4. Esperamos

Después de algunos segundos tendremos disponible la aplicación, volvemos a Loadmill y clickamos en verificar.