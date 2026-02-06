REFAKTORERINGSBESLUT - Dependency Injection

     Koden var från början hårdkodad och anropade direkt specifika klasser (ex. PaymentApi.charge()/ DatabaseConnection.getInstance())
     vilket gjorde den begränsad och svår att modifiera i ett framtida skede.
     Nu när processorn istället tar emot sina tjänster via konstruktorn kan vi med lätthet byta ut dessa i ett framtida skede utan att behöva ändra processorns logik.

     Processorn var tidigare tvungen att veta om exakta tekniska detaljer såsom SQL-syntax och API-nyckel, men nu när den
     bara pratar med interfacen är fokuset endast på flödet/affärslogiken. Vi har nu separerat vad som ska göras från hur det utförs.

     Tidigare hade vi behövt en aktiv databas och riktig internetuppkoppling vilket gjorde vår kod svår att testa.
     Nu kan vi istället injicera "Mock-objekt" för att simulera olika scenarier i en isolerad miljö.