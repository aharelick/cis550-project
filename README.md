<img src="https://upload.wikimedia.org/wikipedia/en/5/5e/Hoaxed_photo_of_the_Loch_Ness_monster.jpg" align="right" />
# Nessie
> Data lake management system.

The final project for CIS550 (Database and Information Systems) at the University of Pennsylvania. The code we deployed on heroku and used in our demo can be found on the [deployed](https://github.com/aharelick/cis550-project/tree/deployed) branch.


## Running the Web Interface
- Clone/download the project
- `cd` into the `nessie-webapp` folder just inside the root of the project
```zsh
$ npm install
```
- Make sure MongoDB and Redis are running locally
- Create a config file named /nessie-webapp-config/config.js from the template below
```javascript
module.exports = {
  MONGODB_URI: 'mongodb://localhost:27017/nessie',
  SESSION_SECRET: 'INSERT SECRET KEY HERE',
  AWS_ACCESS_KEY_ID: 'INSERT AWS ACCESS KEY ID HERE',
  AWS_SECRET_ACCESS_KEY: 'INSERT AWS SECRET ACCESS KEY HERE',
  REDIS_URL: 'redis://localhost:6379'
};

`$ npm start` or `$ npm run dev` (uses [nodemon](https://github.com/remy/nodemon))

- Navigate to `localhost:3000` in a browser


## Contributers
- [Alex Harelick](https://github.com/aharelick)
- [Anjali Khetan](https://github.com/anjalikhetan)
- [Joey Raso](https://github.com/joeyraso)
- [Max Tromanhauser](https://github.com/mtrom)
