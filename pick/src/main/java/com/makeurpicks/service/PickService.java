package com.makeurpicks.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.makeurpicks.domain.DoublePick;
import com.makeurpicks.domain.Pick;
import com.makeurpicks.exception.PickValidationException;
import com.makeurpicks.exception.PickValidationException.PickExceptions;
import com.makeurpicks.game.GameIntegrationService;
import com.makeurpicks.game.GameResponse;
import com.makeurpicks.league.LeagueIntegrationService;
import com.makeurpicks.league.LeagueResponse;
import com.makeurpicks.repository.DoublePickRepository;
import com.makeurpicks.repository.PickRepository;
import com.makeurpicks.repository.PicksByWeekRepository;

@Component
public class PickService {

	@Autowired
	private PickRepository pickRepository;
	
	@Autowired
	private GameIntegrationService gameIntegrationService;
	
	@Autowired 
	private LeagueIntegrationService leagueIntegrationService;
	
	@Autowired
	private DoublePickRepository doublePickRepository;
	
	@Autowired
	private PicksByWeekRepository picksByWeekRepository;

	



	public void setLeagueIntegrationService(LeagueIntegrationService leagueIntegrationService) {
		this.leagueIntegrationService = leagueIntegrationService;
	}



	public void setGameIntegrationService(GameIntegrationService gameIntegrationService) {
		this.gameIntegrationService = gameIntegrationService;
	}



	public Pick makePick(Pick pick)
	{
		//make sure all the parms are set
		validatePick(pick);  
		
		pick.setId(UUID.randomUUID().toString());
		
		//save pick by pick id
		pickRepository.save(pick);
		
		picksByWeekRepository.createPick(pick);
		//save the pick so it can be assessed by league and week
//		picksByLeagueWeekRepository.addPick(pick);
		
		//save the pick so it can be assessed by league, week, and player
//		picksByLeagueWeekAndPlayerRepository.addPick(pick);
		
		return pick;
	}


	public Pick updatePick(Pick pick, String loggedInPlayerId)
	{
		//make sure all the parms are set
		validatePick(pick);  
		
		Pick pickFromDS = pickRepository.findOne(pick.getId());
		if (pickFromDS.getPlayerId() != loggedInPlayerId)
			throw new PickValidationException(PickExceptions.UNAUTHORIZED_USER);
		
		//save pick by pick id
		pickRepository.save(pick);
		
		return pick;
	}
	
	public Map<String, Pick>getPicksByWeekAndPlayer(String weekId, String playerId)
	{
		Map<String, Map<String, String>> map = picksByWeekRepository.getPlayersByWeek(weekId);
		return getPicksByWeekAndPlayer(map, weekId, playerId);
	}
	
	public Map<String, Pick> getOtherPicksByWeekAndPlayer(String weekId, String playerId)
	{
		Map<String, Pick> picks = getPicksByWeekAndPlayer(weekId, playerId);
//		Map<String, Pick> filteredpicks = new HashMap<>(picks.size());
//		gameClient.
//		for ()
		
		return picks;
	}
	
	private Map<String, Pick>getPicksByWeekAndPlayer(Map<String, Map<String, String>> map, String weekId, String playerId)
	{
//		Map<String, Map<String, String>> map = picksByWeekRepository.getPlayersByWeek(weekId);
		if (map==null)
			return Collections.emptyMap();
		Map<String, String> games = map.get(playerId);
		Set<String> subkeys = games.keySet();
		
		Map<String, Pick> pickMap = new HashMap<>();
		for (String gameId : subkeys)
		{
			String pickId = games.get(gameId);
			Pick pick = pickRepository.findOne(pickId);
			pickMap.put(gameId, pick);
			
		}
		
		return pickMap;
		
	}
	
	public Map<String, Map<String, Pick>>getPicksByWeek(String weekId)
	{
		Map<String, Map<String, String>> map = picksByWeekRepository.getPlayersByWeek(weekId);
		if (map==null)
			return Collections.emptyMap();
		
		Set<String>players = map.keySet();
		
		Map<String, Map<String, Pick>> gameMap = new HashMap<>();
		for (String playerId:players)
		{
			
			Map<String, Pick> pickMap = getPicksByWeekAndPlayer(map, weekId, playerId);
			gameMap.put(playerId, pickMap);
			
		}
		
		
		return gameMap;
	}
	
//	public Iterable<Pick> getPicksByLeagueAndWeek(String leagueId, String weekId)
//	{ 
//		Iterable<String> ids = picksByLeagueWeekRepository.getPicksForLeagueAndWeek(leagueId, weekId);
//		return pickRepository.findAll(ids);
//	}
//	
//	public Iterable<Pick> getPicksByLeagueWeekAndPlayer(String leagueId, String weekId, String playerId)
//	{ 
//		Iterable<String> ids = picksByLeagueWeekAndPlayerRepository.getPicksForLeagueWeekAndPlayer(leagueId, weekId, playerId);
//		return pickRepository.findAll(ids);
//	}
	
	
	
	private List<LeagueResponse> getLeaguesForPlayer(String playerId) 
	{
		return leagueIntegrationService.getLeaguesForPlayer(playerId);
	}
	
	
	private void validatePick(Pick pick)
	{
		List<PickExceptions> codes = new ArrayList<PickExceptions>();
		
		
		if (pick == null)
		{
			throw new PickValidationException(PickExceptions.PICK_IS_NULL);
		}
		
		if ("".equals(pick.getGameId()))
			codes.add(PickExceptions.GAME_IS_NULL);
		
		if ("".equals(pick.getTeamId()))
			codes.add(PickExceptions.TEAM_IS_NULL);
		
		if ("".equals(pick.getWeekId()))
			codes.add(PickExceptions.WEEK_IS_NULL);
		
//		if ("".equals(pick.getLeagueId()))
//			codes.add(PickExceptions.LEAGUE_IS_NULL);
		
//		/*if (pick.getLeagueId()==null)
//			cod*/es.add(PickExceptions.LEAGUE_IS_NULL);
		
		if ("".equals(pick.getPlayerId()))
			codes.add(PickExceptions.PLAYER_IS_NUll);
		
		
		GameResponse game = gameIntegrationService.getGameById(pick.getGameId());
//		Game game = dao.loadByPrimaryKey(Game.class, pick.getGame().getId());
		if (game == null)
			codes.add(PickExceptions.GAME_IS_NULL);
		
		//load the game to make sure that the team passed is actually playing in the game
//		if (game.getFav().getId()!=pick.getTeam().getId() && game.getDog().getId()!=pick.getTeam().getId())
		if (!game.getFavId().equals(pick.getTeamId()) && !game.getDogId().equals(pick.getTeamId()))
			codes.add(PickExceptions.TEAM_NOT_PLAYING_IN_GAME);
				
		//check to make sure that the game hasn't started
		if (game.getHasGameStarted())
			codes.add(PickExceptions.GAME_HAS_ALREADY_STARTED);
		
		//need to make sure that the user is in that league
//		List<LeagueResponse> leagues = getLeaguesForPlayer(pick.getPlayerId());
//		boolean playerExistsInLeague = false;
//		for (LeagueResponse league: leagues)
//		{
//			if (league.getId().equals(pick.getLeagueId()))
//			{
//				playerExistsInLeague = true;
//				break;
//			}
//		}
		
//		if (!playerExistsInLeague)
//			codes.add(PickExceptions.PLAYER_NOT_IN_LEAGUE);
		
		//make sure the week matches the game
//		if (game.getWeek().getId()!=pick.getWeek().getId())
		if (!game.getWeekId().equals(pick.getWeekId()))
			codes.add(PickExceptions.WEEK_IS_NOT_VALID);
				
		if (!codes.isEmpty())
			throw new PickValidationException(codes.toArray(new PickExceptions[codes.size()]));
	}
	
	
	public DoublePick getDoublePick(String weekId, String playerId)
	{
		return doublePickRepository.findOne(DoublePick.buildString(weekId, playerId));
	}

	public DoublePick makeDoublePick(String pickId, String loggedInPlayerId)
	{
//		Picks pick = dao.loadByPrimaryKey(Picks.class, pickId);
		Pick pick = pickRepository.findOne(pickId);
		if (pick==null)
			throw new PickValidationException(PickExceptions.PICK_IS_NULL);
		
		GameResponse game = gameIntegrationService.getGameById(pick.getGameId());
		//check to see if the newly selected double has started
		if (game.getHasGameStarted())
		{
			//you can't change to a game that has started
			throw new PickValidationException(PickExceptions.GAME_HAS_ALREADY_STARTED);
		}
		
		if (!pick.getPlayerId().equals(loggedInPlayerId))
			throw new PickValidationException(PickExceptions.UNAUTHORIZED_USER);
		
//		Picks oldPick = getDoublePickForPlayerLeagueAndWeek(pick.getName(), pick.getLeague(), pick.getWeek());
		DoublePick orginialDoublePick = doublePickRepository.findOne(DoublePick.buildString(pick.getWeekId(), pick.getPlayerId())); 
		
		//need to check to see if the orginal pick game has started
		if (orginialDoublePick!=null)
		{
			String orginalPickId = orginialDoublePick.getPickId();
			Pick orginalPick = pickRepository.findOne(orginalPickId);
			GameResponse orginalGame = gameIntegrationService.getGameById(orginalPick.getGameId());
			if (orginalGame.getHasGameStarted())
			{
				throw new PickValidationException(PickExceptions.GAME_HAS_ALREADY_STARTED);
			}
			
			//update the to the new pick and update the repo
			orginialDoublePick.setPickId(pickId);
			doublePickRepository.save(orginialDoublePick);
			
			return orginialDoublePick;
		}
		else
		{
			//there is no orginal pick, so create a new one
			DoublePick doublePick = new DoublePick(pick.getWeekId(), pick.getPlayerId(), pickId, game.getId(), game.getHasGameStarted());
			doublePickRepository.save(doublePick);
			
			return doublePick;
		}
		
		
		
	}
}
