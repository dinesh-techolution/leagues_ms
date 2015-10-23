package com.makeurpicks.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.makeurpicks.domain.Team;
import com.makeurpicks.service.TeamService;

@RequestMapping(value="/teams")
@RestController
public class TeamController {

	@Autowired
	private TeamService teamService;
	
	@RequestMapping(method=RequestMethod.GET, value="/leaguetype/{leagueType}")
	public @ResponseBody List<Team> getTeams(@PathVariable String leagueType)
	{
		return teamService.getTeams(leagueType);
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/{id}")
	public @ResponseBody Team getTeamById(@PathVariable String id)
	{
		return teamService.getTeam(id);
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/")
	public @ResponseBody Team createTeam(@RequestBody Team team) {
		return teamService.createTeam(team);
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/pickem")
	public @ResponseBody List<Team> createPickemTeams() {
		return teamService.createTeams("pickem");
	}
}
